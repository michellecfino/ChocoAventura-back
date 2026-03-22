package com.chocoaventura.DTOs;

import java.util.Set;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class PerfilResponseDTO {
    private Long id;
    private Double presupuesto;
    private Integer personasCargo;
    private Integer tiempoDiarioActividades;
    private Boolean faseIndividualLista;
    private String codigoVuelo;
    private UsuarioResponseDTO usuario;
    private GrupoViajeResponseDTO grupoViaje;
    private Set<CategoriaResponseDTO> categoriasPreferidas;
    private Set<ActividadResponseDTO> actividadesSeleccionadas;
}