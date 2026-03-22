package com.chocoaventura.Services;


import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.DTOs.UnirseGrupoDTO;
import com.chocoaventura.Repositories.CategoriaRepository;
import com.chocoaventura.Repositories.GrupoRepository;
import com.chocoaventura.Repositories.UsuarioRepository;
import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Categoria;
import com.chocoaventura.entities.Ciudad;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.Ubicacion;
import com.chocoaventura.entities.Usuario;

@Service
public class GrupoViajeService {
    public GrupoViaje crearGrupoViaje(String nombre, String nombreDestino, String PaisDestino, String direccion, double lat, double longi, LocalDateTime fechaInicio, LocalDateTime fechaFin, String descripcion,LocalTime horaAlmuerzo, LocalTime horaInicioActividades,Integer tiempoParaAlmorzar) {
        /*if (nombre==null){
            try {
                throw new IllegalArgumentException("El nombre del grupo de viaje no puede ser nulo");
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }
        }*/

        // puedo hacer las validaciones en el front de que si manden todos los valores y si no mande error y asi en esta parte me ahorro las avalidadicones 
        // La unica que hago aqui es la de descripcion porque es la unica que puede ser nula y si es nula le asigno un valor por defecto para asi evitar errores en el futuro cuando se quiera mostrar la descripcion del grupo de viaje y esta sea nula
        

        if (descripcion==null) {
            descripcion = "Este es el viaje para " + nombreDestino + " desde " + fechaInicio.toString() + " hasta " + fechaFin.toString() ;
        }
        /*
        La hora del almuerzo por ahora la define el usuario que crea el grupo de viaje pero 
        en el futuro se puede hacer una votacion para definir la hora del almuerzo y la hora de inicio de las actividades 
        y asi tener una hora más justa para todos los usuarios del grupo de viaje. Por ahora asumamos que son felices y  no pelean
         */
        Ubicacion estadia = new Ubicacion(nombreDestino,direccion, lat, longi);
        Ciudad destino = new Ciudad(nombreDestino, PaisDestino,estadia);
        GrupoViaje grupoViaje = new GrupoViaje(nombre, descripcion, fechaInicio, fechaFin, destino, horaAlmuerzo, horaInicioActividades, tiempoParaAlmorzar);
          /*-------------------------------------------------
            IMPORTANTE 
            ------------------------------------------------- 
            En esta parte al usuario sde le debe preguntar personalmente:
            sus gustos )categorias que prefiere más para este viaje)
            SU presupuesto para el viaje
            Cuantas personas tiene acargo ejemplo es un padre con dos hijos tiene a cargo a dos personas (para hacer el calculo)
            El tiempo que tiene disponible para el viaje para las actividades (para hacer el calculo de las actividades que se pueden hacer en el viaje)

            ESTO SE HACE DESPUES DE CREAR EL GRUPO DE VIAJE Y CUANDO EL USUARIO QUIERE UNIRSE A UN GRUPO DE VIAJE 
             */
        return grupoViaje;
    }

    public void crearPerfilParGrupoViaje(Usuario usuario, GrupoViaje grupoViaje, List<Categoria> categoriasPreferidas, double presupuesto, int personasACargo, int tiempoDisponible) {
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

        Set<Perfil> perfiles = grupoViaje.getPerfiles();
        perfiles.add(perfil);   
        grupoViaje.setPerfiles(perfiles);

        Set<Perfil> perfilesUsuario = usuario.getPerfiles();       
        perfilesUsuario.add(perfil);
        usuario.setPerfiles(perfilesUsuario);

        perfil.setGrupoViaje(grupoViaje);
        perfil.setUsuario(usuario);

    }   

    @Autowired
    private GrupoRepository grupoRepository;
    @Autowired
    private UsuarioRepository usuarioRepository;
    @Autowired
    private CategoriaRepository categoriaRepository;

    public void unirseAGrupoViaje(UnirseGrupoDTO dto) {

    Usuario usuario = usuarioRepository.findById(dto.getUsuarioId()).orElseThrow();
    GrupoViaje grupo = grupoRepository.findById(dto.getGrupoId()).orElseThrow();

    List<Categoria> categorias = categoriaRepository.findAllById(dto.getCategoriasIds());

   
    crearPerfilParGrupoViaje(
        usuario,
        grupo,
        categorias,
        dto.getPresupuesto(),
        dto.getPersonasACargo(),
        dto.getTiempoDisponible()
    );

    // Guardar 
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
