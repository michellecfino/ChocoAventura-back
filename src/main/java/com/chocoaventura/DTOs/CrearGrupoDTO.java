package com.chocoaventura.DTOs;

import java.time.LocalDateTime;
import java.time.LocalTime;

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

    //  USUARIO CREADOR
    private Long duenoId;
}