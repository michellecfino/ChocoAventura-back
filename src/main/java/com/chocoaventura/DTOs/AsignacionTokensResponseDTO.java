package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AsignacionTokensResponseDTO {
    private Long id;
    private Integer tokensAsignados;
    private Long perfilId;
    private Long actividadId;
    private Long rondaSubastaId;
}