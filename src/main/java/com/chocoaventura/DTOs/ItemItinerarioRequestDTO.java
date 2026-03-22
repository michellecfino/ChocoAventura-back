package com.chocoaventura.DTOs;

import java.time.LocalDateTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItemItinerarioRequestDTO {
    private LocalDateTime inicioProgramado;
    private LocalDateTime finProgramado;
    private String estado;
    private Long itinerarioId;
    private Long actividadId;
}