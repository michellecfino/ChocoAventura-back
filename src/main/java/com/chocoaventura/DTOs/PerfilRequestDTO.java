package com.chocoaventura.DTOs;

import java.util.Set;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PerfilRequestDTO {
    private Double presupuesto;
    private Integer personasCargo;
    private Integer tiempoDiarioActividades;
    private Boolean faseIndividualLista;
    private String codigoVuelo;
    private Long usuarioId;
    private Long grupoViajeId;
    private Set<Long> categoriasPreferidasIds;
    private Set<Long> actividadesSeleccionadasIds;
}