package com.chocoaventura.DTOs;

import java.time.format.DateTimeFormatter;

import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.Itinerario;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * DTO de respuesta diseñado para que Flutter pueda deserializarlo
 * directamente en GrupoViajeModel (grupo_viaje_model.dart).
 *
 * Campos obligatorios en Flutter:
 *   id, nombreViaje, destinoKey, destinoNombre, ciudadDepartamento,
 *   fechaInicio, fechaFin, participantes, estadoDisplay, faseActual,
 *   itinerarioEstado, codigoInvitacion, linkInvitacion
 */
@Getter
@AllArgsConstructor
public class GrupoViajeFlutterDTO {

    private Long id;

    /** Nombre del grupo → GrupoViajeModel.nombreViaje */
    private String nombreViaje;

    /**
     * Clave de destino lowercase sin tildes, para mapear assets Flutter.
     * Ej: "cartagena", "medellin", "bogota".
     * Se genera normalizando el nombre de la ciudad.
     */
    private String destinoKey;

    /** Nombre de la ciudad destino para mostrar en UI */
    private String destinoNombre;

    /** Ciudad + país para mostrar como subtítulo (ej: "Cartagena, Colombia") */
    private String ciudadDepartamento;

    /** ISO-8601 datetime string: "2025-07-10T08:00:00" */
    private String fechaInicio;

    /** ISO-8601 datetime string */
    private String fechaFin;

    /** Cantidad de perfiles/participantes en el grupo */
    private int participantes;

    /** Texto de estado para UI: "Activo", "Cerrado", "En espera"… */
    private String estadoDisplay;

    /**
     * Fase del flujo — valores válidos en Flutter (ViajeFaseProducto.name):
     * crearViaje | invitar | preferencias | explorarActividades |
     * esperaGrupoVotacion | priorizacionCategorias | mesaChoco |
     * itinerarioGenerado | ajustesItinerario | viajeActivo | gastosViaje
     */
    private String faseActual;

    /** Texto de estado del itinerario: "En construcción", "Listo", … */
    private String itinerarioEstado;

    /** Id del itinerario persistido más reciente del viaje, si existe. */
    private Long itinerarioId;

    /** Texto corto de la próxima actividad, si el itinerario ya existe. */
    private String proximaActividadTexto;

    /** Código corto para invitar: "CHOCO-ABC123" */
    private String codigoInvitacion;

    /** URL completa de invitación */
    private String linkInvitacion;

    private Boolean usuarioActualExploro;
    private Boolean usuarioActualPriorizo;
    private Boolean puedeExplorar;
    private Boolean puedePriorizar;
    private Integer faltanExploracion;
    private Integer faltanPriorizacion;

    // ------------------------------------------------------------------
    // Factory
    // ------------------------------------------------------------------

    private static final DateTimeFormatter ISO = DateTimeFormatter.ISO_LOCAL_DATE_TIME;

    public static GrupoViajeFlutterDTO fromEntity(GrupoViaje g) {
        String ciudad = g.getCiudadDestino() != null ? g.getCiudadDestino().getNombre() : "";
        String pais   = g.getCiudadDestino() != null ? g.getCiudadDestino().getPais()   : "";

        String destinoKey  = normalizar(ciudad);
        String destinoNom  = ciudad;
        String ciudadDpto  = ciudad.isBlank() ? "" : ciudad + (pais.isBlank() ? "" : ", " + pais);

        String inicio = g.getFechaHoraLlegada() != null ? g.getFechaHoraLlegada().format(ISO) : null;
        String fin    = g.getFechaHoraSalida()  != null ? g.getFechaHoraSalida().format(ISO)  : null;

        int participantes = g.getPerfiles() != null ? g.getPerfiles().size() : 0;

        String estadoDisplay = mapearEstado(g.getEstado() != null ? g.getEstado().name() : "ABIERTO");
        String faseActual    = resolverFase(g);
        String itinEstado    = g.getItinerarios() != null && !g.getItinerarios().isEmpty()
                ? "Listo" : "En construcción";

        // Código e invitación: el servicio genera el link; aquí usamos valores derivados
        String codigo = "CHOCO-" + g.getId();
        String link   = "https://chocoaventura.app/invitacion/" + codigo;

        Itinerario itinerarioActual = obtenerItinerarioActual(g);
        Long itinerarioId = itinerarioActual != null ? itinerarioActual.getId() : null;
        String proximaActividadTexto = resolverProximaActividad(itinerarioActual);

        return new GrupoViajeFlutterDTO(
                g.getId(), g.getNombre(), destinoKey, destinoNom, ciudadDpto,
                inicio, fin, participantes, estadoDisplay, faseActual, itinEstado,
                itinerarioId, proximaActividadTexto,
                codigo, link,
                null, null, null, null, null, null
        );
    }

    public static GrupoViajeFlutterDTO fromEntityForUsuario(
            GrupoViaje g,
            Perfil perfilUsuario,
            boolean todosExploraron,
            boolean usuarioPriorizo,
            boolean todosPriorizaron) {
        return fromEntityForUsuario(g, perfilUsuario, todosExploraron, usuarioPriorizo, todosPriorizaron, null);
    }

    public static GrupoViajeFlutterDTO fromEntityForUsuario(
            GrupoViaje g,
            Perfil perfilUsuario,
            boolean todosExploraron,
            boolean usuarioPriorizo,
            boolean todosPriorizaron,
            Integer faltanPriorizacion) {
        GrupoViajeFlutterDTO base = fromEntity(g);
        boolean usuarioExploro = perfilUsuario != null && Boolean.TRUE.equals(perfilUsuario.getFaseIndividualLista());
        int total = g.getPerfiles() != null ? (int) g.getPerfiles().stream()
                .filter(p -> Boolean.TRUE.equals(p.getParticipaEnCoordinacion()))
                .count() : 0;
        int explorados = g.getPerfiles() != null ? (int) g.getPerfiles().stream()
                .filter(p -> Boolean.TRUE.equals(p.getParticipaEnCoordinacion()))
                .filter(p -> Boolean.TRUE.equals(p.getFaseIndividualLista()))
                .count() : 0;
        String fase = resolverFaseUsuario(g, usuarioExploro, todosExploraron, usuarioPriorizo, todosPriorizaron);
        boolean puedeExplorar = "explorarActividades".equals(fase);
        boolean puedePriorizar = "priorizacionCategorias".equals(fase);
        int participantesActivos = total > 0 ? total : base.participantes;
        Integer faltanPriorizacionSeguro = faltanPriorizacion != null
                ? Math.max(0, faltanPriorizacion)
                : (todosPriorizaron ? 0 : null);
        return new GrupoViajeFlutterDTO(
                base.id,
                base.nombreViaje,
                base.destinoKey,
                base.destinoNombre,
                base.ciudadDepartamento,
                base.fechaInicio,
                base.fechaFin,
                participantesActivos,
                base.estadoDisplay,
                fase,
                base.itinerarioEstado,
                base.itinerarioId,
                base.proximaActividadTexto,
                base.codigoInvitacion,
                base.linkInvitacion,
                usuarioExploro,
                usuarioPriorizo,
                puedeExplorar,
                puedePriorizar,
                Math.max(0, total - explorados),
                faltanPriorizacionSeguro
        );
    }


    private static Itinerario obtenerItinerarioActual(GrupoViaje g) {
        if (g.getItinerarios() == null || g.getItinerarios().isEmpty()) return null;
        return g.getItinerarios().stream()
                .filter(i -> i != null && i.getId() != null)
                .max((a, b) -> a.getId().compareTo(b.getId()))
                .orElse(null);
    }

    private static String resolverProximaActividad(Itinerario itinerario) {
        if (itinerario == null || itinerario.getItems() == null || itinerario.getItems().isEmpty()) return null;
        return itinerario.getItems().stream()
                .filter(item -> item != null && item.getInicioProgramado() != null && item.getActividad() != null)
                .sorted((a, b) -> a.getInicioProgramado().compareTo(b.getInicioProgramado()))
                .map(item -> item.getActividad().getNombre())
                .filter(nombre -> nombre != null && !nombre.isBlank())
                .findFirst()
                .orElse(null);
    }

    // ------------------------------------------------------------------
    // Helpers privados
    // ------------------------------------------------------------------

    private static String resolverFaseUsuario(
            GrupoViaje g,
            boolean usuarioExploro,
            boolean todosExploraron,
            boolean usuarioPriorizo,
            boolean todosPriorizaron) {
        if (g.getItinerarios() != null && !g.getItinerarios().isEmpty()) return "itinerarioGenerado";
        if (g.getEstado() != null && "ITINERARIO_GENERADO".equals(g.getEstado().name())) return "itinerarioGenerado";
        if (!usuarioExploro) return "explorarActividades";
        if (!todosExploraron) return "esperaGrupoVotacion";
        if (!usuarioPriorizo) return "priorizacionCategorias";
        if (!todosPriorizaron) return "esperaGrupoVotacion";
        return "mesaChoco";
    }

    /** Normaliza nombre de ciudad a clave de asset Flutter (minúscula, sin tildes, sin espacios) */
    private static String normalizar(String texto) {
        if (texto == null || texto.isBlank()) return "destino";
        return texto.toLowerCase()
                .replace("á", "a").replace("é", "e").replace("í", "i")
                .replace("ó", "o").replace("ú", "u").replace("ü", "u")
                .replace(" ", "_")
                .replaceAll("[^a-z0-9_]", "");
    }

    private static String mapearEstado(String estado) {
        return switch (estado) {
            case "ABIERTO" -> "Activo";
            case "CONFIRMACION_GRUPAL_PENDIENTE" -> "Esperando al grupo";
            case "COORDINACION_ACTIVA" -> "Mesa de Choco";
            case "ITINERARIO_GENERADO" -> "Itinerario listo";
            case "FINALIZADO" -> "Finalizado";
            default -> "Activo";
        };
    }

    /**
     * Mapea el estado del grupo al nombre de la fase Flutter.
     * Ajusta esta lógica según tu enum EstadoGrupoViaje.
     */
    private static String resolverFase(GrupoViaje g) {
        if (g.getEstado() == null) return "explorarActividades";
        return switch (g.getEstado().name()) {
            case "ABIERTO" -> "explorarActividades";
            case "CONFIRMACION_GRUPAL_PENDIENTE" -> "esperaGrupoVotacion";
            case "COORDINACION_ACTIVA" -> "mesaChoco";
            case "ITINERARIO_GENERADO" -> "itinerarioGenerado";
            case "FINALIZADO" -> "viajeActivo";
            default -> "explorarActividades";
        };
    }
}
