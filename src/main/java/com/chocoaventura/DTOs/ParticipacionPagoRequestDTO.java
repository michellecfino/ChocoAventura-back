package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ParticipacionPagoRequestDTO {
    private Double montoPagado;
    private Double porcentajeResponsabilidad;
    private Long perfilId;
    private Long pagoId;
}