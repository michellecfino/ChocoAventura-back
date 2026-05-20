package com.chocoaventura.DTOs;

import java.util.List;

import lombok.Data;

@Data
public class UnirseGrupoDTO {

    private Long usuarioId;
    private Long grupoId;
    private String codigoInvitacion;

    // Datos del perfil (la relación)
    private List<Long> categoriasIds;
    private double presupuesto;
    private int personasACargo;
}