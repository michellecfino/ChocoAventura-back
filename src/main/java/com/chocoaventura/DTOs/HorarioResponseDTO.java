package com.chocoaventura.DTOs;

import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class HorarioResponseDTO {
    private Long id;
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Long actividadId;
    private DiaSemanaResponseDTO diasSemana;
}