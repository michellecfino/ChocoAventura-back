package com.chocoaventura.DTOs;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PagoRequestDTO {
    private String nombre;
    private Double montoTotal;
    private LocalDate fecha;
    private Long grupoViajeId;
}