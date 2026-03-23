package com.chocoaventura.DTOs;

import lombok.Data;
import java.util.List;

@Data
public class UnirseGrupoDTO {

    private Long usuarioId;
    private Long grupoId;

    // Datos del perfil (la relación)
    private List<Long> categoriasIds;
    private double presupuesto;
    private int personasACargo;
    private int tiempoDisponible;
}