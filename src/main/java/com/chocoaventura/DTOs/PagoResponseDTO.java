package com.chocoaventura.DTOs;

import java.time.LocalDate;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PagoResponseDTO {
    private Long id;
    private String nombre;
    private Double montoTotal;
    private LocalDate fecha;
    private GrupoViajeResponseDTO grupoViaje;
}