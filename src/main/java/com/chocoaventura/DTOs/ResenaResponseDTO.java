package com.chocoaventura.DTOs;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResenaResponseDTO {
    private Long id;
    private Integer calificacion;
    private String comentario;
    private LocalDate fecha;
    private PerfilResponseDTO perfil;
    private ActividadResponseDTO actividad;
}