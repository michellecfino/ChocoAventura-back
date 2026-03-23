package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItinerarioRequestDTO {
    private String nombre;
    private Double presupuestoPromedioPersona;
    private Long grupoViajeId;
}