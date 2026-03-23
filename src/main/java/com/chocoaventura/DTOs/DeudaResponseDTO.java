package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DeudaResponseDTO {
    private Long id;
    private Double monto;
    private Boolean saldada;
    private PerfilResponseDTO deudor;
    private PerfilResponseDTO acreedor;
    private Long pagoOrigenId;
}