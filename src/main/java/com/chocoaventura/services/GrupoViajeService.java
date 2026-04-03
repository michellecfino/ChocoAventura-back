package com.chocoaventura.services;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.DTOs.CrearGrupoDTO;
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
        dto.getTiempoParaAlmorzar(),
        dto.getFechaInicio(),
        dto.getFechaFin(),
        ciudad,
        dueno
    );

    grupo.setEstadia(estadia);
    grupo.setEstado(EstadoGrupoViaje.ABIERTO);

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

    public void unirseAGrupoViaje(UnirseGrupoDTO dto) {
        Usuario usuario = usuarioRepository.findById(dto.getUsuarioId()).orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado con id: " + dto.getUsuarioId()));
        GrupoViaje grupo = grupoRepository.findById(dto.getGrupoId()).orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado con id: " + dto.getGrupoId()));

        if (perfilRepository.existsByUsuarioIdAndGrupoViajeId(dto.getUsuarioId(), dto.getGrupoId())) {
            throw new IllegalArgumentException("El usuario ya pertenece a este grupo de viaje.");
        }

        List<Categoria> categorias = dto.getCategoriasIds() == null ? List.of() : categoriaRepository.findAllById(dto.getCategoriasIds());
        Set<Categoria> categoriasSet = new HashSet<>(categorias);

        boolean participaEnCoordinacion = grupo.getEstado() == EstadoGrupoViaje.ABIERTO
                || grupo.getEstado() == EstadoGrupoViaje.CONFIRMACION_GRUPAL_PENDIENTE;

        crearPerfilParGrupoViaje(
                usuario,
                grupo,
                categoriasSet,
                dto.getPresupuesto(),
                dto.getPersonasACargo(),
                dto.getTiempoDisponible(),
                participaEnCoordinacion
        );

        grupoRepository.save(grupo);
        usuarioRepository.save(usuario);
    }

    public String generarLinkInvitacion(Long grupoId) {
        return "chocoaventura://grupo/" + grupoId;
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