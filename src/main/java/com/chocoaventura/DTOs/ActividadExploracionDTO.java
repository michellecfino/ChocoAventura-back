package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ActividadExploracionDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double costoPorPersona;
    private Integer duracionMin;
    private Double calificacionPromedio;
    private String familiaPlan;
    private String zona;
    private Integer interesTotal;
    private List<String> propuestoPor;
}
