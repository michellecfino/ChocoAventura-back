package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ParticipacionPagoResponseDTO {
    private Long id;
    private Double montoPagado;
    private Double porcentajeResponsabilidad;
    private Long perfilId;
    private Long pagoId;
}