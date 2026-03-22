package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AsignacionTokensRequestDTO {
    private Integer tokensAsignados;
    private Long perfilId;
    private Long actividadId;
    private Long rondaSubastaId;
}