package com.chocoaventura.services;

import java.text.Normalizer;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.DTOs.CrearGrupoDTO;
import com.chocoaventura.DTOs.GrupoViajeFlutterDTO;
import com.chocoaventura.DTOs.UnirseGrupoDTO;
import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Categoria;
import com.chocoaventura.entities.Ciudad;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.Ubicacion;
import com.chocoaventura.entities.Usuario;
import com.chocoaventura.entities.enums.EstadoGrupoViaje;
import com.chocoaventura.repositories.CategoriaRepository;
import com.chocoaventura.repositories.CiudadRepository;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.PerfilRepository;
import com.chocoaventura.repositories.PrioridadCategoriaGrupoRepository;
import com.chocoaventura.repositories.UbicacionRepository;
import com.chocoaventura.repositories.UsuarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class GrupoViajeService {

    @Autowired
    private GrupoViajeRepository grupoRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Autowired
    private CategoriaRepository categoriaRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PrioridadCategoriaGrupoRepository prioridadCategoriaGrupoRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    // =========================
    // CRUD básico
    // =========================

    public GrupoViaje create(GrupoViaje grupoViaje) {
        return grupoRepository.save(grupoViaje);
    }

    public List<GrupoViaje> getAll() {
        return grupoRepository.findAll();
    }

    public GrupoViaje getById(Long id) {
        return grupoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + id));
    }

    public GrupoViaje update(Long id, GrupoViaje datos) {
        GrupoViaje grupo = getById(id);

        grupo.setNombre(datos.getNombre());
        grupo.setDescripcion(datos.getDescripcion());
        grupo.setHoraInicioActividades(datos.getHoraInicioActividades());
        grupo.setHoraAlmuerzo(datos.getHoraAlmuerzo());
        grupo.setDuracionAlmuerzoMin(datos.getDuracionAlmuerzoMin());
        grupo.setFechaHoraLlegada(datos.getFechaHoraLlegada());
        grupo.setFechaHoraSalida(datos.getFechaHoraSalida());
        grupo.setCiudadDestino(datos.getCiudadDestino());
        grupo.setEstadia(datos.getEstadia());
        grupo.setDueno(datos.getDueno());
        if (datos.getEstado() != null) {
            grupo.setEstado(datos.getEstado());
        }
        return grupoRepository.save(grupo);
    }

    public void delete(Long id) {
        GrupoViaje grupo = getById(id);
        grupoRepository.delete(grupo);
    }

    // =========================
    // Lógica
    // =========================

    public GrupoViaje crearGrupoViaje(CrearGrupoDTO dto) {

    String descripcion = dto.getDescripcion();
    if (descripcion == null) {
        descripcion = "Viaje a " + dto.getNombreCiudad() +
                " desde " + dto.getFechaInicio() +
                " hasta " + dto.getFechaFin();
    }

    //  Validar ciudad
    if (dto.getNombreCiudad() == null || dto.getPaisCiudad() == null) {
        throw new IllegalArgumentException("Datos de ciudad incompletos");
    }

    //  Buscar o crear ciudad
    List<Ciudad> ciudades = ciudadRepository
        .findByNombreIgnoreCaseAndPaisIgnoreCase(
            dto.getNombreCiudad(),
            dto.getPaisCiudad()
        );

    Ciudad ciudad;

    if (ciudades.isEmpty()) {
        ciudad = new Ciudad(
                dto.getNombreCiudad(),
                dto.getPaisCiudad()
        );
        ciudad = ciudadRepository.save(ciudad);
    } else {
        ciudad = ciudades.get(0);
    }

    //  Estadía opcional
    Ubicacion estadia = null;

    boolean hayDatosEstadia =
        dto.getNombreEstadia() != null &&
        dto.getDireccionEstadia() != null &&
        dto.getLatEstadia() != null &&
        dto.getLngEstadia() != null;

    if (hayDatosEstadia) {

        List<Ubicacion> estadias = ubicacionRepository
            .findByDireccionAndLatitudAndLongitud(
                dto.getDireccionEstadia(),
                dto.getLatEstadia(),
                dto.getLngEstadia()
            );

        if (estadias.isEmpty()) {
            estadia = new Ubicacion(
                    dto.getNombreEstadia(),
                    dto.getDireccionEstadia(),
                    dto.getLatEstadia(),
                    dto.getLngEstadia()
            );
            estadia = ubicacionRepository.save(estadia);
        } else {
            estadia = estadias.get(0);
        }
    }

    //  Usuario
    Usuario dueno = usuarioRepository.findById(dto.getDuenoId())
        .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

    //  Crear grupo
    GrupoViaje grupo = new GrupoViaje(
        dto.getNombre(),
        descripcion,
        dto.getHoraInicioActividades(),
        dto.getHoraAlmuerzo(),
        dto.duracionAlmuerzoEfectiva(),
        dto.getFechaInicio(),
        dto.getFechaFin(),
        ciudad,
        dueno
    );

    grupo.setEstadia(estadia);
    grupo.setEstado(EstadoGrupoViaje.ABIERTO);

    grupo = grupoRepository.save(grupo);

    // El front espera que el dueño del grupo también aparezca como participante/perfil.
    // Sin este perfil, el creador no podía ver su viaje en /grupos/usuarios/{usuarioId}
    // ni completar la exploración individual.
    if (!perfilRepository.existsByUsuarioIdAndGrupoViajeId(dueno.getId(), grupo.getId())) {
        Set<Categoria> categoriasCreador = resolverCategoriasPreferidas(dto.getCategoriasIds(), dto.getCategoriasPreferidas());
        Perfil perfilDueno = crearPerfilParGrupoViaje(
                dueno,
                grupo,
                categoriasCreador,
                dto.presupuestoEfectivo(),
                dto.personasACargoEfectivas(),
                8,
                true
        );
        perfilRepository.save(perfilDueno);
    }

    return grupoRepository.save(grupo);

}

    public Perfil crearPerfilParGrupoViaje(Usuario usuario, GrupoViaje grupoViaje, Set<Categoria> categoriasPreferidas, double presupuesto, int personasACargo, int tiempoDisponible, boolean participaEnCoordinacion) {
        // Lógica para crear un perfil para un grupo de viaje
        /*
            ------------------------------------    
            IMPORTANTE
            ------------------------------------
            En esta parte se le pide al usuario qeu seleccione las categorias y llene otro tipo de formulario para asi tener toda la info 
            De nuevo esto va en el front 
            o 
        */

        Perfil perfil = new Perfil(presupuesto, personasACargo, tiempoDisponible, categoriasPreferidas);
        perfil.setGrupoViaje(grupoViaje);
        perfil.setUsuario(usuario);
        perfil.setParticipaEnCoordinacion(participaEnCoordinacion);
        if (!participaEnCoordinacion) {
            perfil.setFaseIndividualLista(true);
        }

        grupoViaje.getPerfiles().add(perfil);
        usuario.getPerfiles().add(perfil);
        return perfil;
    }

    public GrupoViaje unirseAGrupoViaje(UnirseGrupoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId())
                .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + dto.getUsuarioId()));
        GrupoViaje grupo = resolverGrupoParaUnion(dto);

        List<Categoria> categorias = dto.getCategoriasIds() == null ? List.of() : categoriaRepository.findAllById(dto.getCategoriasIds());
        Set<Categoria> categoriasSet = new HashSet<>(categorias);

        List<Perfil> perfilesExistentes = perfilRepository.findAllByUsuarioIdAndGrupoViajeId(dto.getUsuarioId(), grupo.getId());
        if (!perfilesExistentes.isEmpty()) {
            Perfil perfil = perfilesExistentes.get(0);
            if (dto.getPresupuesto() > 0) {
                perfil.setPresupuesto(dto.getPresupuesto());
            }
            if (dto.getPersonasACargo() > 0) {
                perfil.setPersonasCargo(Math.max(1, dto.getPersonasACargo()));
            }
            perfil.setCategoriasPreferidas(categoriasSet);
            perfil.setParticipaEnCoordinacion(true);
            perfilRepository.save(perfil);
            return grupoRepository.findById(grupo.getId()).orElse(grupo);
        }

        if (grupo.getEstado() == EstadoGrupoViaje.ITINERARIO_GENERADO
                || grupo.getEstado() == EstadoGrupoViaje.FINALIZADO) {
            throw new IllegalStateException("No es posible unirse a un viaje que ya tiene itinerario generado o está finalizado.");
        }

        boolean participaEnCoordinacion = true;

        crearPerfilParGrupoViaje(
                usuario,
                grupo,
                categoriasSet,
                dto.getPresupuesto(),
                Math.max(1, dto.getPersonasACargo()),
                8,
                participaEnCoordinacion
        );

        // Si alguien entra después de que el grupo ya había avanzado, el progreso grupal
        // debe recalcularse con el nuevo participante. También se invalidan votos anteriores
        // de Mesa de Choco, porque el conjunto de categorías puede cambiar con sus swipes.
        if (grupo.getEstado() == EstadoGrupoViaje.CONFIRMACION_GRUPAL_PENDIENTE
                || grupo.getEstado() == EstadoGrupoViaje.COORDINACION_ACTIVA) {
            grupo.setEstado(EstadoGrupoViaje.ABIERTO);
            prioridadCategoriaGrupoRepository.deleteAll(
                    prioridadCategoriaGrupoRepository.findByGrupoViajeId(grupo.getId())
            );
        }

        grupo = grupoRepository.save(grupo);
        usuarioRepository.save(usuario);
        return grupoRepository.findById(grupo.getId()).orElse(grupo);
    }

    public GrupoViaje buscarPorCodigoInvitacion(String codigo) {
        Long grupoId = extraerGrupoIdDesdeCodigo(codigo);
        return grupoRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("No existe un viaje con ese código."));
    }

    private GrupoViaje resolverGrupoParaUnion(UnirseGrupoDTO dto) {
        if (dto.getGrupoId() != null) {
            return grupoRepository.findById(dto.getGrupoId())
                    .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con id: " + dto.getGrupoId()));
        }
        if (dto.getCodigoInvitacion() != null && !dto.getCodigoInvitacion().isBlank()) {
            return buscarPorCodigoInvitacion(dto.getCodigoInvitacion());
        }
        throw new IllegalArgumentException("Debes enviar grupoId o codigoInvitacion para unirte al viaje.");
    }

    private Long extraerGrupoIdDesdeCodigo(String codigoOriginal) {
        if (codigoOriginal == null || codigoOriginal.isBlank()) {
            throw new IllegalArgumentException("Código de invitación vacío.");
        }
        String codigo = codigoOriginal.trim();
        int slash = Math.max(codigo.lastIndexOf('/'), codigo.lastIndexOf('\\'));
        if (slash >= 0 && slash + 1 < codigo.length()) {
            codigo = codigo.substring(slash + 1);
        }
        codigo = codigo.trim().toUpperCase();
        if (codigo.startsWith("CHOCO-")) {
            codigo = codigo.substring("CHOCO-".length());
        }
        String soloDigitos = codigo.replaceAll("[^0-9]", "");
        if (soloDigitos.isBlank()) {
            throw new IllegalArgumentException("Código de invitación inválido.");
        }
        return Long.parseLong(soloDigitos);
    }

    public List<GrupoViajeFlutterDTO> listarGruposPorUsuario(Long usuarioId) {
        List<Perfil> perfiles = perfilRepository.findByUsuarioId(usuarioId);

        return perfiles.stream()
                .filter(p -> p.getGrupoViaje() != null)
                .map(p -> GrupoViajeFlutterDTO.fromEntityForUsuario(
                        p.getGrupoViaje(),
                        p,
                        todosExploraron(p.getGrupoViaje().getId()),
                        usuarioPriorizo(p.getGrupoViaje().getId(), p),
                        todosPriorizaron(p.getGrupoViaje().getId()),
                        faltanPorPriorizar(p.getGrupoViaje().getId())
                ))
                .toList();
    }


    public GrupoViajeFlutterDTO toFlutterDTOForUsuario(GrupoViaje grupo, Long usuarioId) {
        if (grupo == null) {
            throw new EntityNotFoundException("Grupo de viaje no encontrado");
        }
        Perfil perfil = null;
        if (usuarioId != null && grupo.getId() != null) {
            perfil = perfilRepository.findAllByUsuarioIdAndGrupoViajeId(usuarioId, grupo.getId())
                    .stream()
                    .findFirst()
                    .orElse(null);
        }
        return GrupoViajeFlutterDTO.fromEntityForUsuario(
                grupo,
                perfil,
                todosExploraron(grupo.getId()),
                usuarioPriorizo(grupo.getId(), perfil),
                todosPriorizaron(grupo.getId()),
                faltanPorPriorizar(grupo.getId())
        );
    }

    private Set<Categoria> resolverCategoriasPreferidas(List<Long> categoriaIds, List<String> nombresCategorias) {
        Set<Categoria> resultado = new HashSet<>();
        if (categoriaIds != null && !categoriaIds.isEmpty()) {
            resultado.addAll(categoriaRepository.findAllById(categoriaIds));
        }
        if (nombresCategorias != null) {
            for (String nombre : nombresCategorias) {
                if (nombre == null || nombre.isBlank()) continue;
                Categoria categoria = buscarCategoriaPorNombreOAlias(nombre);
                if (categoria != null) resultado.add(categoria);
            }
        }
        return resultado;
    }

    private Categoria buscarCategoriaPorNombreOAlias(String nombre) {
        String solicitada = normalizarTexto(nombre);
        if (solicitada.isBlank()) return null;
        Optional<Categoria> exacta = categoriaRepository.findByNombreIgnoreCase(nombre.trim());
        if (exacta.isPresent()) return exacta.get();
        for (Categoria categoria : categoriaRepository.findAll()) {
            String disponible = normalizarTexto(categoria.getNombre());
            if (disponible.equals(solicitada) || disponible.contains(solicitada) || solicitada.contains(disponible)) {
                return categoria;
            }
            if (coincideAliasCategoria(solicitada, disponible)) return categoria;
        }
        return null;
    }

    private boolean coincideAliasCategoria(String solicitada, String disponible) {
        if ((solicitada.contains("naturaleza") || solicitada.contains("aventura")) && disponible.contains("aventura")) return true;
        if (solicitada.contains("cultura") && disponible.contains("cultura")) return true;
        if (solicitada.contains("gastronomia") && disponible.contains("gastronomia")) return true;
        if ((solicitada.contains("noche") || solicitada.contains("fiesta")) && disponible.contains("nocturna")) return true;
        if (solicitada.contains("playa") && disponible.contains("playa")) return true;
        if (solicitada.contains("local") && (disponible.contains("local") || disponible.contains("autentica"))) return true;
        if (solicitada.contains("foto") && disponible.contains("autentica")) return true;
        if (solicitada.contains("relax") && disponible.contains("relax")) return true;
        return false;
    }

    private String normalizarTexto(String texto) {
        if (texto == null) return "";
        String sinTildes = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        return sinTildes.toLowerCase().trim();
    }

    private boolean todosExploraron(Long grupoId) {
        List<Perfil> participantes = perfilRepository.findByGrupoViajeIdAndParticipaEnCoordinacionTrue(grupoId);
        return !participantes.isEmpty() && participantes.stream().allMatch(p -> Boolean.TRUE.equals(p.getFaseIndividualLista()));
    }

    private boolean usuarioPriorizo(Long grupoId, Perfil perfil) {
        if (perfil == null || perfil.getId() == null) return false;
        return !prioridadCategoriaGrupoRepository.findByGrupoViajeIdAndPerfilIdOrderByPosicionAsc(grupoId, perfil.getId()).isEmpty();
    }

    private boolean todosPriorizaron(Long grupoId) {
        List<Perfil> participantes = perfilRepository.findByGrupoViajeIdAndParticipaEnCoordinacionTrue(grupoId);
        if (participantes.isEmpty()) return false;
        for (Perfil participante : participantes) {
            if (!usuarioPriorizo(grupoId, participante)) return false;
        }
        return true;
    }

    private int faltanPorPriorizar(Long grupoId) {
        List<Perfil> participantes = perfilRepository.findByGrupoViajeIdAndParticipaEnCoordinacionTrue(grupoId);
        if (participantes.isEmpty()) return 0;
        int priorizados = 0;
        for (Perfil participante : participantes) {
            if (participante.getId() == null) continue;
            if (!prioridadCategoriaGrupoRepository
                    .findByGrupoViajeIdAndPerfilIdOrderByPosicionAsc(grupoId, participante.getId())
                    .isEmpty()) {
                priorizados++;
            }
        }
        return Math.max(0, participantes.size() - priorizados);
    }

    public String generarLinkInvitacion(Long grupoId) {
        return "https://chocoaventura.app/invitacion/CHOCO-" + grupoId;
    }

    /*
FLUJO INVITACIÓN CON DEEP LINK (Flutter + Spring Boot)

1. BACKEND (Spring Boot):
   - Genera link de invitación:
     chocoaventura://grupo/{grupoId}
   - Expone endpoint:
     POST /grupos/unirse
   - NO maneja navegación ni decisiones de usuario

2. FRONTEND (Flutter):
   - Recibe el deep link (grupoId)

   - Verifica si el usuario está logueado:
        - NO logueado:
            → guardar grupoId
            → redirigir a login/registro
            → después del login, continuar flujo

        - SÍ logueado:
            → ir directo a pantalla "Unirse al grupo"

   - Mostrar formulario para crear Perfil:
        (presupuesto, categorías, tiempo, etc.)

   - Al enviar:
        → llamar POST /grupos/unirse con UnirseGrupoDTO

3. IMPORTANTE:
   - El backend NO decide si el usuario se registra o no
   - El frontend controla todo el flujo y navegación
   - El deep link solo indica a qué grupo quiere unirse

4. MVP:
   - Usar deep links simples
   - Solo funciona si la app está instalada
*/

    public void registrarPago() {
        // Lógica para registrar un pago en un grupo de viaje
    }

    public void hacerSubasta() {
        // Lógica para realizar una subasta en un grupo de viaje
    }

    public void hacerItinerario() {
        // Lógica para crear un itinerario en un grupo de viaje
    }

    public void votarActividad(Usuario usuario, Actividad actividad) {
        // Lógica para votar por una actividad en un grupo de viaje
    }

    public void obtenerListaActividadesVotadas() {
        // Lógica para obtener la lista de actividades votadas en un grupo de viaje
    }

    public void hacerseleccionDeActividadesParaSubasta() {
        // Lógica para hacer la selección de actividades para una subasta en un grupo de viaje
        // estoo es teniedno en cuenta las actividades seleccionadas por cada usuario y haciendo una simp´lificacion para asi tener una lista de actividades más pequeña para la subasta 
        // con el algoritmo que definimos para tener cllaro como se haran esas selecciones de actividades para la subasta
        // Debe depender de una cantidad de días y el presupuesto de cada usuario para asi hacer una selección de actividades más justa para la subasta
    }

    public void SeleccionarActividadesFinales() {
        // Lógica para seleccionar las actividades finales para el grupo de viaje después de la subasta
    }

    public void obtenerItinerarioFinal() {
        // Lógica para obtener el itinerario final del grupo de viaje después de la selección de actividades finales
    }
}