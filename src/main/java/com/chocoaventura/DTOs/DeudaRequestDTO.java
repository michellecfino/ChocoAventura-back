package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DeudaRequestDTO {
    private Double monto;
    private Boolean saldada;
    private Long deudorId;
    private Long acreedorId;
    private Long pagoOrigenId;
}