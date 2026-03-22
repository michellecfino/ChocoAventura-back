package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ItinerarioResponseDTO {
    private Long id;
    private String nombre;
    private Double presupuestoPromedioPersona;
    private GrupoViajeResponseDTO grupoViaje;
}