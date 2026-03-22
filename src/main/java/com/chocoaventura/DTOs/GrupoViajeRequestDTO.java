package com.chocoaventura.DTOs;

import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GrupoViajeRequestDTO {
    private String nombre;
    private String descripcion;
    private LocalTime horaInicioActividades;
    private LocalTime horaAlmuerzo;
    private Integer duracionAlmuerzoMin;
    private LocalDateTime fechaHoraLlegada;
    private LocalDateTime fechaHoraSalida;
    private Long ciudadDestinoId;
    private Long estadiaId;
    private Long duenoId;
}