package com.chocoaventura.DTOs;

import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class HorarioRequestDTO {
    private LocalTime horaInicio;
    private LocalTime horaFin;
    private Long actividadId;
    private Long diasSemanaId;
}