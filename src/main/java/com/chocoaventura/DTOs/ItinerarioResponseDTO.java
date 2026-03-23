package com.chocoaventura.DTOs;

import java.util.List;

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
    private List<DiaItinerarioDTO> dias;
}