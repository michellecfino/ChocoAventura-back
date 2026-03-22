package com.chocoaventura.DTOs;

import java.time.LocalDateTime;
import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class RondaSubastaResponseDTO {
    private Long id;
    private LocalDateTime fechaFin;
    private String estado;
    private Integer tokensPorPerfil;
    private GrupoViajeResponseDTO grupoViaje;
    private Set<ActividadResponseDTO> actividadesSubasta;
}