package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ItinerarioRequestDTO {
    private String nombre;
    private Long grupoViajeId;
}