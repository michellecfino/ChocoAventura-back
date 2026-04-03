package com.chocoaventura.DTOs;

import java.time.LocalDate;
import java.time.LocalDateTime;
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
    private String fuenteUrl;
    private String imagenUrl;
    private String categoria;
    private Double latitud;
    private Double longitud;
    private LocalDateTime fechaCreacion;
    private CiudadResponseDTO ciudad;
    private UbicacionResponseDTO ubicacion;
    private Set<CategoriaResponseDTO> categorias;
    private Set<ImagenResponseDTO> imagenes;
}