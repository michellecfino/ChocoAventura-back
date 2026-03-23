package com.chocoaventura.DTOs;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResenaRequestDTO {
    private Integer calificacion;
    private String comentario;
    private LocalDate fecha;
    private Long perfilId;
    private Long actividadId;
}