package com.chocoaventura.services;

import com.chocoaventura.DTOs.*;
import com.chocoaventura.entities.*;
import com.chocoaventura.entities.enums.EstadoGrupoViaje;
import com.chocoaventura.repositories.BloqueExploracionGrupalRepository;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.PerfilRepository;
import com.chocoaventura.repositories.RondaSubastaRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class ExploracionGrupalService {

    private static final int MAX_PRINCIPALES_POR_RONDA = 20;
    private static final Set<String> STOPWORDS = Set.of(
            "de", "la", "el", "en", "y", "con", "por", "para", "del", "los", "las", "un", "una", "a", "al"
    );

    @Autowired
    private GrupoViajeRepository grupoViajeRepository;
    @Autowired
    private PerfilRepository perfilRepository;
    @Autowired
    private RondaSubastaRepository rondaSubastaRepository;
    @Autowired
    private BloqueExploracionGrupalRepository bloqueRepository;
    @Autowired
    private RondaSubastaService rondaSubastaService;

    public EstadoExploracionGrupalDTO evaluarEstado(Long grupoId) {
        GrupoViaje grupo = obtenerGrupo(grupoId);
        List<Perfil> perfilesDecisores = perfilRepository.findByGrupoViajeIdAndParticipaEnCoordinacionTrue(grupoId);
        int total = perfilesDecisores.size();
        int listos = (int) perfilesDecisores.stream().filter(Perfil::getFaseIndividualLista).count();
        boolean todosListos = total > 0 && total == listos;

        if (grupo.getEstado() == EstadoGrupoViaje.ABIERTO && todosListos) {
            grupo.setEstado(EstadoGrupoViaje.CONFIRMACION_GRUPAL_PENDIENTE);
            grupoViajeRepository.save(grupo);
        }

        String mensaje;
        boolean requiereConfirmacion = false;
        if (total == 0) {
            mensaje = "Aún no hay participantes activos para la coordinación.";
        } else if (!todosListos) {
            mensaje = "Faltan participantes por terminar la exploración individual y confirmar sus actividades.";
        } else if (grupo.getEstado() == EstadoGrupoViaje.CONFIRMACION_GRUPAL_PENDIENTE) {
            requiereConfirmacion = true;
            mensaje = "Todos los participantes actuales ya terminaron. Falta que el dueño confirme si ya quiere iniciar la exploración grupal.";
        } else if (grupo.getEstado() == EstadoGrupoViaje.COORDINACION_ACTIVA || grupo.getEstado() == EstadoGrupoViaje.ITINERARIO_GENERADO) {
            mensaje = "La coordinación grupal ya está activa para este viaje.";
        } else {
            mensaje = "El grupo sigue abierto y puede seguir recibiendo participantes.";
        }

        return new EstadoExploracionGrupalDTO(
                grupoId,
                grupo.getEstado().name(),
                todosListos,
                requiereConfirmacion,
                total,
                listos,
                mensaje
        );
    }

    @Transactional
    public EstadoExploracionGrupalDTO confirmarInicioCoordinacion(Long grupoId, Long duenoId, boolean confirmar) {
        GrupoViaje grupo = obtenerGrupo(grupoId);
        if (!Objects.equals(grupo.getDueno().getId(), duenoId)) {
            throw new IllegalArgumentException("Solo el dueño del viaje puede confirmar el inicio de la coordinación grupal.");
        }

        EstadoExploracionGrupalDTO estado = evaluarEstado(grupoId);
        if (!estado.isTodosLosPerfilesListos()) {
            throw new IllegalStateException("No se puede confirmar la fase grupal hasta que todos los participantes decisores terminen su fase individual.");
        }

        if (!confirmar) {
            grupo.setEstado(EstadoGrupoViaje.ABIERTO);
            grupoViajeRepository.save(grupo);
            return new EstadoExploracionGrupalDTO(
                    grupoId,
                    grupo.getEstado().name(),
                    true,
                    false,
                    estado.getTotalPerfilesDecisores(),
                    estado.getPerfilesListos(),
                    "El dueño decidió seguir esperando. Avísales a los demás compañeros que se unan y completen sus preferencias."
            );
        }

        grupo.setEstado(EstadoGrupoViaje.COORDINACION_ACTIVA);
        grupo.setFechaConfirmacionCoordinacion(LocalDateTime.now());
        grupoViajeRepository.save(grupo);
        generarExploracionGrupal(grupoId);

        return new EstadoExploracionGrupalDTO(
                grupoId,
                grupo.getEstado().name(),
                true,
                false,
                estado.getTotalPerfilesDecisores(),
                estado.getPerfilesListos(),
                "La coordinación grupal quedó iniciada y las rondas fueron generadas correctamente."
        );
    }

    @Transactional
    public ExploracionGrupalResponseDTO generarExploracionGrupal(Long grupoId) {
        GrupoViaje grupo = obtenerGrupo(grupoId);
        List<Perfil> perfilesDecisores = perfilRepository.findByGrupoViajeIdAndParticipaEnCoordinacionTrue(grupoId);
        validarGrupoListoParaCoordinar(grupo, perfilesDecisores);

        // Limpia rondas previas si se regenera.
        List<RondaSubasta> previas = rondaSubastaRepository.findByGrupoViajeId(grupoId);
        if (!previas.isEmpty()) {
            rondaSubastaRepository.deleteAll(previas);
        }

        Map<Long, CandidateActivity> candidates = construirCandidatos(perfilesDecisores);
        if (candidates.isEmpty()) {
            throw new IllegalStateException("No hay actividades seleccionadas por los participantes decisores para construir la exploración grupal.");
        }

        int numeroRondas = calcularNumeroRondas(grupo);
        int capacidadTotal = numeroRondas * MAX_PRINCIPALES_POR_RONDA;

        List<SimilarityGroup> grupos = agruparPorSimilitud(new ArrayList<>(candidates.values()));
        grupos = comprimirGruposSiExcedeCapacidad(grupos, capacidadTotal);
        grupos.forEach(this::escogerPrincipalYTitulo);
        grupos.sort(Comparator.comparingDouble(SimilarityGroup::groupScore).reversed());

        List<SimilarityGroup> seleccionados = seleccionarGruposRepresentativos(grupos, perfilesDecisores, capacidadTotal);
        List<RondaArmada> rondasArmadas = repartirEnRondas(seleccionados, numeroRondas);
        persistirRondasYBloques(grupo, rondasArmadas);

        return obtenerExploracionGrupal(grupoId);
    }

    public ExploracionGrupalResponseDTO obtenerExploracionGrupal(Long grupoId) {
        GrupoViaje grupo = obtenerGrupo(grupoId);
        List<Perfil> perfilesDecisores = perfilRepository.findByGrupoViajeIdAndParticipaEnCoordinacionTrue(grupoId);
        List<RondaSubasta> rondas = rondaSubastaRepository.findByGrupoViajeId(grupoId).stream()
                .sorted(Comparator.comparing(RondaSubasta::getNumeroRonda))
                .toList();

        Set<Long> perfilesRepresentados = new HashSet<>();
        List<RondaExploracionGrupalDTO> rondasDto = new ArrayList<>();
        for (RondaSubasta ronda : rondas) {
            List<BloqueExploracionGrupal> bloques = bloqueRepository.findByRondaSubastaIdOrderByOrdenVisualAsc(ronda.getId());
            List<BloqueExploracionGrupalDTO> bloquesDto = bloques.stream()
                    .map(b -> {
                        ActividadExploracionDTO principal = toActividadDto(b.getActividadPrincipal());
                        perfilesRepresentados.addAll(b.getActividadPrincipal().getPerfilesQueLaSeleccionaron().stream()
                                .filter(Perfil::getParticipaEnCoordinacion)
                                .map(Perfil::getId)
                                .collect(Collectors.toSet()));
                        List<ActividadExploracionDTO> similares = b.getActividadesSimilares().stream()
                                .map(this::toActividadDto)
                                .toList();
                        return new BloqueExploracionGrupalDTO(
                                b.getId(),
                                b.getTitulo(),
                                b.getFamiliaPlan(),
                                b.getScoreGrupo(),
                                b.getCantidadPerfilesRepresentados(),
                                principal,
                                similares
                        );
                    })
                    .toList();
            rondasDto.add(new RondaExploracionGrupalDTO(
                    ronda.getId(),
                    ronda.getNumeroRonda(),
                    ronda.getBloqueInicio(),
                    ronda.getBloqueFin(),
                    ronda.getTokensPorPerfil(),
                    bloquesDto
            ));
        }

        return new ExploracionGrupalResponseDTO(
                grupoId,
                "Conoce lo que otros propusieron antes de decidir",
                "Explora los planes más representativos del grupo y, si quieres, abre ver similares para descubrir alternativas parecidas sin saturarte.",
                grupo.getEstado().name(),
                perfilesDecisores.size(),
                perfilesRepresentados.size(),
                rondasDto
        );
    }

    private void validarGrupoListoParaCoordinar(GrupoViaje grupo, List<Perfil> perfilesDecisores) {
        if (grupo.getEstado() != EstadoGrupoViaje.COORDINACION_ACTIVA && grupo.getEstado() != EstadoGrupoViaje.ITINERARIO_GENERADO) {
            throw new IllegalStateException("El grupo todavía no ha sido confirmado para entrar a la fase grupal.");
        }
        if (perfilesDecisores.isEmpty()) {
            throw new IllegalStateException("No hay participantes decisores en este viaje.");
        }
        boolean todosListos = perfilesDecisores.stream().allMatch(Perfil::getFaseIndividualLista);
        if (!todosListos) {
            throw new IllegalStateException("No todos los participantes decisores han completado la fase individual.");
        }
    }

    private Map<Long, CandidateActivity> construirCandidatos(List<Perfil> perfiles) {
        Map<Long, CandidateActivity> candidates = new LinkedHashMap<>();
        for (Perfil perfil : perfiles) {
            for (Actividad actividad : perfil.getActividadesSeleccionadas()) {
                CandidateActivity candidate = candidates.computeIfAbsent(actividad.getId(), id -> new CandidateActivity(actividad));
                candidate.selectorIds.add(perfil.getId());
                candidate.selectorNames.add(perfil.getUsuario().getNombre());
                candidate.familia = inferirFamiliaPlan(actividad);
                candidate.conceptos = extraerConceptos(actividad);
                candidate.tokensCrudos = extraerTokensNormalizados(actividad.getNombre() + " " + Optional.ofNullable(actividad.getDescripcion()).orElse(""));
                candidate.zona = normalizar(Optional.ofNullable(actividad.getUbicacion()).map(Ubicacion::getNombre).orElse("sin-zona"));
                candidate.priceBucket = bucketPrecio(actividad.getCostoPorPersona());
                candidate.durationBucket = bucketDuracion(actividad.getDuracionMin());
                candidate.baseScore = candidate.selectorIds.size() * 100.0 + Optional.ofNullable(actividad.getCalificacionPromedio()).orElse(0.0) * 10.0;
            }
        }
        return candidates;
    }

    private List<SimilarityGroup> agruparPorSimilitud(List<CandidateActivity> candidates) {
        List<SimilarityGroup> groups = new ArrayList<>();
        for (CandidateActivity candidate : candidates) {
            SimilarityGroup best = null;
            double bestScore = 0.0;
            for (SimilarityGroup group : groups) {
                double similarity = similarity(candidate, group.centroid());
                if (similarity > bestScore) {
                    bestScore = similarity;
                    best = group;
                }
            }
            if (best != null && bestScore >= 0.45) {
                best.activities.add(candidate);
            } else {
                SimilarityGroup group = new SimilarityGroup();
                group.activities.add(candidate);
                groups.add(group);
            }
        }
        return groups;
    }

    private List<SimilarityGroup> comprimirGruposSiExcedeCapacidad(List<SimilarityGroup> groups, int capacity) {
        List<SimilarityGroup> result = new ArrayList<>(groups);
        while (result.size() > capacity && result.size() > 1) {
            double best = -1.0;
            int bestI = -1;
            int bestJ = -1;
            for (int i = 0; i < result.size(); i++) {
                for (int j = i + 1; j < result.size(); j++) {
                    double sim = similarity(result.get(i).centroid(), result.get(j).centroid());
                    if (sim > best) {
                        best = sim;
                        bestI = i;
                        bestJ = j;
                    }
                }
            }
            if (bestI < 0) {
                break;
            }
            SimilarityGroup a = result.get(bestI);
            SimilarityGroup b = result.get(bestJ);
            a.activities.addAll(b.activities);
            result.remove(bestJ);
        }
        return result;
    }

    private void escogerPrincipalYTitulo(SimilarityGroup group) {
        group.activities.sort(Comparator.comparingDouble(CandidateActivity::principalScore).reversed());
        group.main = group.activities.get(0);
        group.familia = group.main.familia;
        group.title = construirTituloAmigable(group);
    }

    private List<SimilarityGroup> seleccionarGruposRepresentativos(List<SimilarityGroup> groups, List<Perfil> perfilesDecisores, int capacityTotal) {
        List<SimilarityGroup> ordered = new ArrayList<>(groups);
        ordered.sort(Comparator.comparingDouble(SimilarityGroup::groupScore).reversed());

        List<SimilarityGroup> selected = new ArrayList<>();
        Map<String, Integer> familyCounts = new HashMap<>();
        Set<Long> coveredProfiles = new HashSet<>();

        for (SimilarityGroup group : ordered) {
            if (selected.size() >= capacityTotal) {
                break;
            }
            int currentFamilyCount = familyCounts.getOrDefault(group.familia, 0);
            int limit = Math.max(2, (int) Math.ceil(capacityTotal * 0.4));
            double bonusCobertura = group.newProfilesCovered(coveredProfiles) * 60.0;
            double penaltyDiversidad = currentFamilyCount >= limit ? 120.0 : 0.0;
            double finalScore = group.groupScore() + bonusCobertura - penaltyDiversidad;
            if (finalScore <= 0) {
                continue;
            }
            selected.add(group);
            familyCounts.put(group.familia, currentFamilyCount + 1);
            coveredProfiles.addAll(group.main.selectorIds);
        }

        Set<Long> perfilesObjetivo = perfilesDecisores.stream()
                .filter(p -> !p.getActividadesSeleccionadas().isEmpty())
                .map(Perfil::getId)
                .collect(Collectors.toSet());

        for (Long perfilId : perfilesObjetivo) {
            if (coveredProfiles.contains(perfilId)) {
                continue;
            }
            Optional<SimilarityGroup> bestGroupForProfile = ordered.stream()
                    .filter(g -> g.main.selectorIds.contains(perfilId) && !selected.contains(g))
                    .max(Comparator.comparingDouble(SimilarityGroup::groupScore));
            if (bestGroupForProfile.isEmpty()) {
                continue;
            }
            if (selected.size() < capacityTotal) {
                selected.add(bestGroupForProfile.get());
                coveredProfiles.addAll(bestGroupForProfile.get().main.selectorIds);
                continue;
            }
            SimilarityGroup replacement = selected.stream()
                    .filter(g -> g.newProfilesCovered(coveredProfiles) == 0)
                    .min(Comparator.comparingDouble(SimilarityGroup::groupScore))
                    .orElse(null);
            if (replacement != null && replacement.groupScore() < bestGroupForProfile.get().groupScore()) {
                selected.remove(replacement);
                selected.add(bestGroupForProfile.get());
                coveredProfiles.clear();
                selected.forEach(g -> coveredProfiles.addAll(g.main.selectorIds));
            }
        }

        selected.sort(Comparator.comparingDouble(SimilarityGroup::groupScore).reversed());
        return selected;
    }

    private List<RondaArmada> repartirEnRondas(List<SimilarityGroup> groups, int numeroRondas) {
        List<RondaArmada> rondas = new ArrayList<>();
        for (int i = 0; i < numeroRondas; i++) {
            rondas.add(new RondaArmada(i + 1));
        }

        for (SimilarityGroup group : groups) {
            RondaArmada best = rondas.stream()
                    .filter(r -> r.groups.size() < MAX_PRINCIPALES_POR_RONDA)
                    .min(Comparator.comparingDouble(r -> r.assignmentCost(group)))
                    .orElseThrow(() -> new IllegalStateException("No fue posible repartir los bloques en las rondas disponibles."));
            best.add(group);
        }
        return rondas;
    }

    private void persistirRondasYBloques(GrupoViaje grupo, List<RondaArmada> rondasArmadas) {
        LocalDate start = grupo.getFechaHoraLlegada().toLocalDate();
        for (RondaArmada rondaArmada : rondasArmadas) {
            LocalDate bloqueInicio = start.plusDays((long) (rondaArmada.numeroRonda - 1) * 7);
            LocalDate bloqueFin = bloqueInicio.plusDays(6);
            if (bloqueFin.isAfter(grupo.getFechaHoraSalida().toLocalDate())) {
                bloqueFin = grupo.getFechaHoraSalida().toLocalDate();
            }
            RondaSubasta ronda = rondaSubastaService.crearRondaParaGrupo(
                    grupo.getId(),
                    rondaArmada.numeroRonda,
                    bloqueInicio,
                    bloqueFin,
                    LocalDateTime.now().plusDays(3)
            );

            int order = 1;
            for (SimilarityGroup group : rondaArmada.groups) {
                ronda.getActividadesSubasta().add(group.main.activity);
                group.activities.stream()
                        .map(c -> c.activity)
                        .forEach(ronda.getActividadesSubasta()::add);

                BloqueExploracionGrupal bloque = new BloqueExploracionGrupal(
                        group.title,
                        group.familia,
                        order++,
                        group.groupScore(),
                        group.representedProfiles().size(),
                        ronda,
                        group.main.activity
                );
                group.activities.stream()
                        .filter(c -> !Objects.equals(c.activity.getId(), group.main.activity.getId()))
                        .map(c -> c.activity)
                        .forEach(bloque.getActividadesSimilares()::add);
                ronda.getBloquesExploracion().add(bloque);
            }
            rondaSubastaRepository.save(ronda);
        }
    }

    private int calcularNumeroRondas(GrupoViaje grupo) {
        long dias = java.time.temporal.ChronoUnit.DAYS.between(
                grupo.getFechaHoraLlegada().toLocalDate(),
                grupo.getFechaHoraSalida().toLocalDate()
        ) + 1;
        return (int) Math.ceil(Math.max(1, dias) / 7.0);
    }

    private ActividadExploracionDTO toActividadDto(Actividad actividad) {
        List<Perfil> perfiles = actividad.getPerfilesQueLaSeleccionaron().stream()
                .filter(Perfil::getParticipaEnCoordinacion)
                .toList();
        return new ActividadExploracionDTO(
                actividad.getId(),
                actividad.getNombre(),
                actividad.getDescripcion(),
                actividad.getCostoPorPersona(),
                actividad.getDuracionMin(),
                actividad.getCalificacionPromedio(),
                inferirFamiliaPlan(actividad),
                Optional.ofNullable(actividad.getUbicacion()).map(Ubicacion::getNombre).orElse("Sin zona"),
                perfiles.size(),
                perfiles.stream().map(p -> p.getUsuario().getNombre()).distinct().toList()
        );
    }

    private GrupoViaje obtenerGrupo(Long grupoId) {
        return grupoViajeRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + grupoId));
    }

    private String inferirFamiliaPlan(Actividad actividad) {
        Set<String> categoriaTokens = actividad.getCategorias().stream()
                .map(Categoria::getNombre)
                .map(this::normalizar)
                .collect(Collectors.toSet());
        String texto = normalizar(actividad.getNombre() + " " + Optional.ofNullable(actividad.getDescripcion()).orElse(""));
        if (contieneAlguno(texto, "bote", "lancha", "barco", "acua", "rio", "manglar", "bahia") || contieneAlguno(categoriaTokens, "acuatico")) {
            return "acuatico";
        }
        if (contieneAlguno(texto, "museo", "galeria", "cultural", "histor", "arte") || contieneAlguno(categoriaTokens, "cultural")) {
            return "cultural";
        }
        if (contieneAlguno(texto, "sendero", "caminata", "trek", "ecologico", "natur") || contieneAlguno(categoriaTokens, "naturaleza")) {
            return "naturaleza";
        }
        if (contieneAlguno(texto, "aventura", "rapel", "tirolesa", "extremo") || contieneAlguno(categoriaTokens, "aventura")) {
            return "aventura";
        }
        if (contieneAlguno(texto, "comida", "gastron", "cocina", "degustacion", "restaurante") || contieneAlguno(categoriaTokens, "gastronomia")) {
            return "gastronomia";
        }
        return actividad.getCategorias().stream().findFirst().map(Categoria::getNombre).map(this::normalizar).orElse("plan_general");
    }

    private Set<String> extraerConceptos(Actividad actividad) {
        String text = normalizar(actividad.getNombre() + " " + Optional.ofNullable(actividad.getDescripcion()).orElse(""));
        Set<String> concepts = new LinkedHashSet<>();
        registrarConcepto(text, concepts, "recorrido_acuatico", "bote", "lancha", "barco", "manglar", "bahia", "acua");
        registrarConcepto(text, concepts, "museo", "museo", "galeria", "arte", "historia", "historico");
        registrarConcepto(text, concepts, "caminata_naturaleza", "sendero", "caminata", "trek", "ecologico", "ruta natural");
        registrarConcepto(text, concepts, "agua_natural", "cascada", "rio", "pozo", "charco", "playa");
        registrarConcepto(text, concepts, "gastronomia", "comida", "degustacion", "cocina", "restaurante", "sabores");
        registrarConcepto(text, concepts, "tour_cultural", "tour", "recorrido", "visita guiada", "cultural");
        if (concepts.isEmpty()) {
            concepts.add(inferirFamiliaPlan(actividad));
        }
        return concepts;
    }

    private void registrarConcepto(String text, Set<String> concepts, String concepto, String... synonyms) {
        for (String synonym : synonyms) {
            if (text.contains(normalizar(synonym))) {
                concepts.add(concepto);
                return;
            }
        }
    }

    private Set<String> extraerTokensNormalizados(String text) {
        return Arrays.stream(normalizar(text).split("\\s+"))
                .filter(token -> !token.isBlank())
                .filter(token -> !STOPWORDS.contains(token))
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String normalizar(String value) {
        return Normalizer.normalize(Optional.ofNullable(value).orElse(""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private String bucketPrecio(Double precio) {
        if (precio == null) return "medio";
        if (precio < 50000) return "bajo";
        if (precio < 120000) return "medio";
        return "alto";
    }

    private String bucketDuracion(Integer duracion) {
        if (duracion == null) return "media";
        if (duracion <= 90) return "corta";
        if (duracion <= 240) return "media";
        return "larga";
    }

    private boolean contieneAlguno(String text, String... values) {
        for (String value : values) {
            if (text.contains(normalizar(value))) return true;
        }
        return false;
    }

    private boolean contieneAlguno(Set<String> tokens, String... values) {
        for (String value : values) {
            if (tokens.contains(normalizar(value))) return true;
        }
        return false;
    }

    private double similarity(CandidateActivity a, CandidateActivity b) {
        double score = 0.0;
        if (Objects.equals(a.familia, b.familia)) score += 0.35;
        score += jaccard(a.conceptos, b.conceptos) * 0.30;
        score += jaccard(a.tokensCrudos, b.tokensCrudos) * 0.15;
        if (Objects.equals(a.priceBucket, b.priceBucket)) score += 0.10;
        if (Objects.equals(a.durationBucket, b.durationBucket)) score += 0.05;
        if (Objects.equals(a.zona, b.zona)) score += 0.05;
        return score;
    }

    private double jaccard(Set<String> a, Set<String> b) {
        if (a.isEmpty() && b.isEmpty()) return 1.0;
        Set<String> inter = new HashSet<>(a);
        inter.retainAll(b);
        Set<String> union = new HashSet<>(a);
        union.addAll(b);
        return union.isEmpty() ? 0.0 : (double) inter.size() / union.size();
    }

    private String construirTituloAmigable(SimilarityGroup group) {
        String concept = group.activities.stream()
                .flatMap(a -> a.conceptos.stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()))
                .entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse(group.familia);

        Map<String, String> friendlyNames = Map.of(
                "recorrido_acuatico", "Recorridos acuáticos",
                "museo", "Museos para explorar",
                "caminata_naturaleza", "Caminatas de naturaleza",
                "agua_natural", "Planes de agua natural",
                "gastronomia", "Experiencias gastronómicas",
                "tour_cultural", "Tours culturales"
        );
        String base = friendlyNames.getOrDefault(concept, capitalizar(group.familia.replace('_', ' ')));
        if (!group.main.zona.equals("sin zona")) {
            return base + " en " + capitalizar(group.main.zona);
        }
        return base;
    }

    private String capitalizar(String value) {
        if (value == null || value.isBlank()) return value;
        return Arrays.stream(value.split(" "))
                .map(w -> w.substring(0, 1).toUpperCase(Locale.ROOT) + w.substring(1))
                .collect(Collectors.joining(" "));
    }

    private static class CandidateActivity {
        private final Actividad activity;
        private final Set<Long> selectorIds = new LinkedHashSet<>();
        private final Set<String> selectorNames = new LinkedHashSet<>();
        private Set<String> conceptos = new LinkedHashSet<>();
        private Set<String> tokensCrudos = new LinkedHashSet<>();
        private String familia;
        private String zona;
        private String priceBucket;
        private String durationBucket;
        private double baseScore;

        private CandidateActivity(Actividad activity) {
            this.activity = activity;
        }

        double principalScore() {
            return selectorIds.size() * 100.0 + Optional.ofNullable(activity.getCalificacionPromedio()).orElse(0.0) * 10.0;
        }
    }

    private static class SimilarityGroup {
        private final List<CandidateActivity> activities = new ArrayList<>();
        private CandidateActivity main;
        private String title;
        private String familia;

        CandidateActivity centroid() {
            return main != null ? main : activities.get(0);
        }

        double groupScore() {
            if (main == null) return 0.0;
            return main.principalScore() + activities.size() * 8.0 + representedProfiles().size() * 12.0;
        }

        Set<Long> representedProfiles() {
            return activities.stream().flatMap(a -> a.selectorIds.stream()).collect(Collectors.toSet());
        }

        int newProfilesCovered(Set<Long> alreadyCovered) {
            Set<Long> covered = representedProfiles();
            covered.removeAll(alreadyCovered);
            return covered.size();
        }
    }

    private static class RondaArmada {
        private final int numeroRonda;
        private final List<SimilarityGroup> groups = new ArrayList<>();
        private final Map<String, Integer> familyCount = new HashMap<>();

        private RondaArmada(int numeroRonda) {
            this.numeroRonda = numeroRonda;
        }

        void add(SimilarityGroup group) {
            groups.add(group);
            familyCount.merge(group.familia, 1, Integer::sum);
        }

        double assignmentCost(SimilarityGroup group) {
            int familyOveruse = familyCount.getOrDefault(group.familia, 0);
            return groups.size() * 20.0 + familyOveruse * 15.0 - group.newProfilesCovered(currentCoveredProfiles()) * 10.0;
        }

        private Set<Long> currentCoveredProfiles() {
            return groups.stream().flatMap(g -> g.representedProfiles().stream()).collect(Collectors.toSet());
        }
    }
}
