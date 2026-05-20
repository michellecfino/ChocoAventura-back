package com.chocoaventura.DTOs;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

import lombok.Data;

@Data
public class CrearGrupoDTO {

    private String nombre;
    private String descripcion;

    // CIUDAD (OBLIGATORIA)
    private String nombreCiudad;
    private String paisCiudad;


    //  ESTADÍA (OPCIONAL)
    private String nombreEstadia;
    private String direccionEstadia;
    private Double latEstadia;
    private Double lngEstadia;

    //  FECHAS
    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    //  CONFIGURACIÓN
    private LocalTime horaAlmuerzo;
    private LocalTime horaInicioActividades;
    private Integer tiempoParaAlmorzar;

    // Alias usado por algunas pantallas Flutter.
    private Integer tiempoAlmuerzoMin;

    // Datos UI que el front puede enviar aunque el modelo persistente los derive de otras entidades.
    private String destinoKey;
    private Double latitud;
    private Double longitud;
    private Double presupuesto;
    private Integer participantes;

    // Preferencias iniciales del creador. Flutter puede enviar nombres simples.
    private List<Long> categoriasIds;
    private List<String> categoriasPreferidas;

    //  USUARIO CREADOR
    private Long duenoId;

    public Integer duracionAlmuerzoEfectiva() {
        if (tiempoParaAlmorzar != null) return tiempoParaAlmorzar;
        if (tiempoAlmuerzoMin != null) return tiempoAlmuerzoMin;
        return 60;
    }

    public Double presupuestoEfectivo() {
        return presupuesto != null ? presupuesto : 500000.0;
    }

    public Integer personasACargoEfectivas() {
        if (participantes == null || participantes <= 1) return 0;
        return participantes - 1;
    }
}