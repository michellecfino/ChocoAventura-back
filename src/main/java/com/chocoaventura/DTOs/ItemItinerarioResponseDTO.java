package com.chocoaventura.DTOs;

import java.time.LocalDateTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ItemItinerarioResponseDTO {
    private Long id;
    private LocalDateTime inicioProgramado;
    private LocalDateTime finProgramado;
    private String estado;
    private Long itinerarioId;
    private ActividadResponseDTO actividad;
}