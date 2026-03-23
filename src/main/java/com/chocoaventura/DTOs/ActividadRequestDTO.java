package com.chocoaventura.DTOs;

import java.time.LocalDate;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ActividadRequestDTO {
    private String nombre;
    private String descripcion;
    private Double costoPorPersona;
    private Integer duracionMin;
    private Double calificacionPromedio;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFin;
    private String preciosDetallados;
    private String fuente;
    private Long ciudadId;
    private Long ubicacionId;
    private Set<Long> categoriasIds;
}