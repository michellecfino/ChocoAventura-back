package com.chocoaventura.DTOs;

import java.time.LocalDate;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ActividadResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private Double costoPorPersona;
    private Integer duracionMin;
    private Double calificacionPromedio;
    private LocalDate vigenciaInicio;
    private LocalDate vigenciaFin;
    private String preciosDetallados;
    private String fuente;
    private CiudadResponseDTO ciudad;
    private UbicacionResponseDTO ubicacion;
    private Set<CategoriaResponseDTO> categorias;
    private Set<ImagenResponseDTO> imagenes;
}