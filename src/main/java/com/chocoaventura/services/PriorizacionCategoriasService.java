package com.chocoaventura.services;

import com.chocoaventura.DTOs.CategoriaPriorizacionDTO;
import com.chocoaventura.DTOs.CategoriaPriorizacionRequestDTO;
import com.chocoaventura.DTOs.CategoriaPriorizacionResponseDTO;
import com.chocoaventura.DTOs.CategoriaRankingItemDTO;
import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Categoria;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.PrioridadCategoriaGrupo;
import com.chocoaventura.repositories.CategoriaRepository;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.PerfilRepository;
import com.chocoaventura.repositories.PrioridadCategoriaGrupoRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@Service
public class PriorizacionCategoriasService {

    @Autowired
    private GrupoViajeRepository grupoViajeRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PrioridadCategoriaGrupoRepository prioridadRepository;

    @Transactional(readOnly = true)
    public CategoriaPriorizacionResponseDTO obtenerOpciones(Long grupoId, Long usuarioId, Long perfilId) {
        GrupoViaje grupo = obtenerGrupo(grupoId);
        Perfil perfil = resolverPerfil(grupo, usuarioId, perfilId, false);
        Map<Long, CategoriaDisponible> disponibles = categoriasDisponiblesDesdeSeleccionadas(grupo);
        List<PrioridadCategoriaGrupo> prioridades = obtenerPrioridadesExistentes(grupoId, perfil);
        return construirRespuesta(grupo, usuarioId, perfil, disponibles, prioridades, "Categorías disponibles para priorizar.");
    }

    @Transactional
    public CategoriaPriorizacionResponseDTO guardarPriorizacion(Long grupoId, CategoriaPriorizacionRequestDTO request) {
        GrupoViaje grupo = obtenerGrupo(grupoId);
        Long usuarioId = request == null ? null : request.getUsuarioId();
        Long perfilId = request == null ? null : request.getPerfilId();
        Perfil perfil = resolverPerfil(grupo, usuarioId, perfilId, false);

        Map<Long, CategoriaDisponible> disponibles = categoriasDisponiblesDesdeSeleccionadas(grupo);
        if (disponibles.isEmpty()) {
            throw new IllegalStateException("No hay categorías para priorizar porque todavía no hay actividades seleccionadas por el grupo.");
        }

        List<Categoria> orden = resolverOrdenCategorias(request, disponibles);
        if (orden.isEmpty()) {
            throw new IllegalArgumentException("Debes enviar al menos una categoría válida para priorizar.");
        }

        if (perfil != null) {
            prioridadRepository.deleteByGrupoViajeIdAndPerfilId(grupoId, perfil.getId());
        } else {
            prioridadRepository.deleteByGrupoViajeIdAndPerfilIsNull(grupoId);
        }

        int total = orden.size();
        List<PrioridadCategoriaGrupo> nuevas = new ArrayList<>();
        for (int i = 0; i < orden.size(); i++) {
            int posicion = i + 1;
            int puntaje = calcularPuntaje(posicion, total);
            nuevas.add(new PrioridadCategoriaGrupo(grupo, perfil, orden.get(i), posicion, puntaje));
        }
        prioridadRepository.saveAll(nuevas);

        return construirRespuesta(grupo, usuarioId, perfil, disponibles, nuevas, "Priorización de categorías guardada correctamente.");
    }

    @Transactional(readOnly = true)
    public Map<Long, Integer> obtenerPuntajesCategoriaParaGrupo(GrupoViaje grupo) {
        if (grupo == null || grupo.getId() == null) {
            return Map.of();
        }
        List<PrioridadCategoriaGrupo> prioridades = prioridadRepository.findByGrupoViajeId(grupo.getId());
        if (prioridades.isEmpty()) {
            return Map.of();
        }
        Map<Long, Integer> puntajes = new HashMap<>();
        for (PrioridadCategoriaGrupo prioridad : prioridades) {
            if (prioridad.getCategoria() == null || prioridad.getCategoria().getId() == null) continue;
            puntajes.merge(prioridad.getCategoria().getId(), valorSeguro(prioridad.getPuntaje()), Integer::sum);
        }
        return puntajes;
    }

    @Transactional(readOnly = true)
    public void validarGrupoListoParaItinerario(GrupoViaje grupo) {
        if (grupo == null || grupo.getId() == null) {
            throw new IllegalArgumentException("Grupo de viaje inválido para generar itinerario.");
        }
        EstadoPriorizacion estado = calcularEstadoPriorizacion(grupo.getId());
        if (estado.totalParticipantes == 0) {
            throw new IllegalStateException("No se puede crear el itinerario porque el viaje no tiene participantes activos.");
        }
        if (!estado.listoParaItinerario()) {
            throw new IllegalStateException(
                    "No se puede crear el itinerario hasta que todos los participantes del viaje prioricen sus categorías. " +
                    "Faltan " + estado.faltanPorPriorizar() + " participante(s).");
        }
    }

    @Transactional(readOnly = true)
    public boolean grupoListoParaItinerario(Long grupoId) {
        return calcularEstadoPriorizacion(grupoId).listoParaItinerario();
    }

    private GrupoViaje obtenerGrupo(Long grupoId) {
        return grupoViajeRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + grupoId));
    }

    private Perfil resolverPerfil(GrupoViaje grupo, Long usuarioId, Long perfilId, boolean obligatorio) {
        if (grupo == null) return null;
        Optional<Perfil> perfil;
        if (perfilId != null) {
            perfil = perfilRepository.findById(perfilId)
                    .filter(p -> p.getGrupoViaje() != null && Objects.equals(p.getGrupoViaje().getId(), grupo.getId()));
        } else if (usuarioId != null) {
            perfil = perfilRepository.findAllByUsuarioIdAndGrupoViajeId(usuarioId, grupo.getId())
                    .stream()
                    .findFirst();
        } else {
            perfil = Optional.empty();
        }
        if (obligatorio && perfil.isEmpty()) {
            throw new EntityNotFoundException("No se encontró un perfil del usuario dentro de este grupo de viaje.");
        }
        return perfil.orElse(null);
    }

    private Map<Long, CategoriaDisponible> categoriasDisponiblesDesdeSeleccionadas(GrupoViaje grupo) {
        Map<Long, CategoriaDisponible> disponibles = new LinkedHashMap<>();
        if (grupo.getPerfiles() == null) return disponibles;

        grupo.getPerfiles().stream()
                .filter(p -> Boolean.TRUE.equals(p.getParticipaEnCoordinacion()))
                .forEach(perfil -> {
                    if (perfil.getActividadesSeleccionadas() == null) return;
                    for (Actividad actividad : perfil.getActividadesSeleccionadas()) {
                        if (actividad == null || actividad.getCategorias() == null) continue;
                        for (Categoria categoria : actividad.getCategorias()) {
                            if (categoria == null || categoria.getId() == null) continue;
                            CategoriaDisponible actual = disponibles.computeIfAbsent(
                                    categoria.getId(),
                                    id -> new CategoriaDisponible(categoria)
                            );
                            actual.cantidadActividadesSeleccionadas++;
                        }
                    }
                });
        return disponibles;
    }

    private List<PrioridadCategoriaGrupo> obtenerPrioridadesExistentes(Long grupoId, Perfil perfil) {
        if (perfil != null) {
            List<PrioridadCategoriaGrupo> propias = prioridadRepository.findByGrupoViajeIdAndPerfilIdOrderByPosicionAsc(grupoId, perfil.getId());
            if (!propias.isEmpty()) return propias;
        }
        return prioridadRepository.findByGrupoViajeIdAndPerfilIsNullOrderByPosicionAsc(grupoId);
    }

    private List<Categoria> resolverOrdenCategorias(CategoriaPriorizacionRequestDTO request, Map<Long, CategoriaDisponible> disponibles) {
        LinkedHashMap<Long, Categoria> orden = new LinkedHashMap<>();
        if (request == null) return List.of();

        if (request.getRanking() != null && !request.getRanking().isEmpty()) {
            List<CategoriaRankingItemDTO> ranking = new ArrayList<>(request.getRanking());
            ranking.sort(Comparator.comparingInt(item -> item.getPosicion() == null ? Integer.MAX_VALUE : item.getPosicion()));
            for (CategoriaRankingItemDTO item : ranking) {
                Categoria categoria = resolverCategoriaSolicitada(item == null ? null : item.getCategoriaId(), item == null ? null : item.getNombre(), disponibles);
                if (categoria != null) orden.putIfAbsent(categoria.getId(), categoria);
            }
        }

        if (request.getCategoriaIds() != null) {
            for (Long categoriaId : request.getCategoriaIds()) {
                Categoria categoria = resolverCategoriaSolicitada(categoriaId, null, disponibles);
                if (categoria != null) orden.putIfAbsent(categoria.getId(), categoria);
            }
        }

        if (request.getCategorias() != null) {
            for (String nombre : request.getCategorias()) {
                Categoria categoria = resolverCategoriaSolicitada(null, nombre, disponibles);
                if (categoria != null) orden.putIfAbsent(categoria.getId(), categoria);
            }
        }

        return new ArrayList<>(orden.values());
    }

    private Categoria resolverCategoriaSolicitada(Long categoriaId, String nombre, Map<Long, CategoriaDisponible> disponibles) {
        if (categoriaId != null && disponibles.containsKey(categoriaId)) {
            return disponibles.get(categoriaId).categoria;
        }
        String nombreNormalizado = normalizar(nombre);
        if (!nombreNormalizado.isBlank()) {
            for (CategoriaDisponible disponible : disponibles.values()) {
                if (coincideCategoria(nombreNormalizado, disponible.categoria.getNombre())) {
                    return disponible.categoria;
                }
            }
            Optional<Categoria> porNombre = categoriaRepository.findByNombreIgnoreCase(nombre.trim());
            if (porNombre.isPresent() && disponibles.containsKey(porNombre.get().getId())) {
                return porNombre.get();
            }
        }
        return null;
    }

    private boolean coincideCategoria(String solicitadaNormalizada, String categoriaDisponible) {
        String disponibleNormalizada = normalizar(categoriaDisponible);
        if (disponibleNormalizada.equals(solicitadaNormalizada)) return true;
        if (disponibleNormalizada.contains(solicitadaNormalizada) || solicitadaNormalizada.contains(disponibleNormalizada)) return true;

        Map<String, List<String>> alias = Map.of(
                "naturaleza", List.of("naturaleza y aventura"),
                "aventura", List.of("naturaleza y aventura"),
                "cultura", List.of("cultura e historia"),
                "noche", List.of("vida nocturna"),
                "playa", List.of("mar y playa"),
                "vida local", List.of("experiencia local", "experiencias autenticas", "experiencias auténticas"),
                "fotos", List.of("experiencias autenticas", "experiencias auténticas"),
                "bajo costo", List.of("gastronomia", "gastronomía", "experiencia local")
        );
        return alias.getOrDefault(solicitadaNormalizada, List.of()).stream()
                .anyMatch(disponibleNormalizada::equals);
    }

    private CategoriaPriorizacionResponseDTO construirRespuesta(
            GrupoViaje grupo,
            Long usuarioId,
            Perfil perfil,
            Map<Long, CategoriaDisponible> disponibles,
            List<PrioridadCategoriaGrupo> prioridades,
            String mensaje
    ) {
        Map<Long, PrioridadCategoriaGrupo> prioridadPorCategoria = new HashMap<>();
        List<CategoriaRankingItemDTO> ranking = new ArrayList<>();
        for (PrioridadCategoriaGrupo prioridad : prioridades) {
            if (prioridad.getCategoria() == null) continue;
            prioridadPorCategoria.put(prioridad.getCategoria().getId(), prioridad);
            ranking.add(new CategoriaRankingItemDTO(
                    prioridad.getCategoria().getId(),
                    prioridad.getCategoria().getNombre(),
                    prioridad.getPosicion(),
                    prioridad.getPuntaje()
            ));
        }

        List<CategoriaPriorizacionDTO> categoriasDisponibles = disponibles.values().stream()
                .sorted(Comparator.comparing(d -> d.categoria.getNombre(), String.CASE_INSENSITIVE_ORDER))
                .map(disponible -> {
                    PrioridadCategoriaGrupo prioridad = prioridadPorCategoria.get(disponible.categoria.getId());
                    return new CategoriaPriorizacionDTO(
                            disponible.categoria.getId(),
                            disponible.categoria.getNombre(),
                            disponible.categoria.getDescripcion(),
                            disponible.cantidadActividadesSeleccionadas,
                            prioridad == null ? null : prioridad.getPosicion(),
                            prioridad == null ? null : prioridad.getPuntaje()
                    );
                })
                .toList();

        Map<String, Integer> puntajesPorCategoria = new LinkedHashMap<>();
        ranking.stream()
                .sorted(Comparator.comparingInt(item -> item.getPosicion() == null ? Integer.MAX_VALUE : item.getPosicion()))
                .forEach(item -> puntajesPorCategoria.put(item.getNombre(), valorSeguro(item.getPuntaje())));

        EstadoPriorizacion estado = calcularEstadoPriorizacion(grupo.getId());
        boolean usuarioActualPriorizo = !ranking.isEmpty();
        List<Perfil> perfilesCoordinacion = perfilRepository.findByGrupoViajeIdAndParticipaEnCoordinacionTrue(grupo.getId());
        boolean todosExploraron = !perfilesCoordinacion.isEmpty()
                && perfilesCoordinacion.stream().allMatch(p -> Boolean.TRUE.equals(p.getFaseIndividualLista()));

        return new CategoriaPriorizacionResponseDTO(
                grupo.getId(),
                usuarioId,
                perfil == null ? null : perfil.getId(),
                categoriasDisponibles,
                ranking,
                puntajesPorCategoria,
                usuarioActualPriorizo,
                estado.totalParticipantes,
                estado.participantesPriorizados,
                estado.faltanPorPriorizar(),
                estado.listoParaItinerario(),
                usuarioActualPriorizo,
                perfil != null && todosExploraron && !usuarioActualPriorizo,
                perfil != null && todosExploraron && !usuarioActualPriorizo,
                mensaje
        );
    }

    private EstadoPriorizacion calcularEstadoPriorizacion(Long grupoId) {
        List<Perfil> participantes = perfilRepository.findByGrupoViajeIdAndParticipaEnCoordinacionTrue(grupoId);
        int total = participantes.size();
        int priorizados = 0;
        for (Perfil participante : participantes) {
            if (participante.getId() == null) continue;
            if (!prioridadRepository.findByGrupoViajeIdAndPerfilIdOrderByPosicionAsc(grupoId, participante.getId()).isEmpty()) {
                priorizados++;
            }
        }
        return new EstadoPriorizacion(total, priorizados);
    }

    private int calcularPuntaje(int posicion, int total) {
        if (total <= 0 || posicion <= 0) return 0;
        return (int) Math.round(100.0 * (total - posicion + 1) / total);
    }

    private int valorSeguro(Integer valor) {
        return valor == null ? 0 : valor;
    }

    private String normalizar(String texto) {
        return texto == null ? "" : texto.trim().toLowerCase(Locale.ROOT);
    }

    private static class EstadoPriorizacion {
        private final int totalParticipantes;
        private final int participantesPriorizados;

        private EstadoPriorizacion(int totalParticipantes, int participantesPriorizados) {
            this.totalParticipantes = totalParticipantes;
            this.participantesPriorizados = participantesPriorizados;
        }

        private int faltanPorPriorizar() {
            return Math.max(0, totalParticipantes - participantesPriorizados);
        }

        private boolean listoParaItinerario() {
            return totalParticipantes > 0 && participantesPriorizados >= totalParticipantes;
        }
    }

    private static class CategoriaDisponible {
        private final Categoria categoria;
        private int cantidadActividadesSeleccionadas;

        private CategoriaDisponible(Categoria categoria) {
            this.categoria = categoria;
            this.cantidadActividadesSeleccionadas = 0;
        }
    }
}
