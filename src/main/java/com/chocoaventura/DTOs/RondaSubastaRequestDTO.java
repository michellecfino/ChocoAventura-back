package com.chocoaventura.DTOs;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RondaSubastaRequestDTO {
    private LocalDateTime fechaFin;
    private String estado;
    private Integer tokensPorPerfil;
    private Long grupoViajeId;
    private Set<Long> actividadesSubastaIds;
}