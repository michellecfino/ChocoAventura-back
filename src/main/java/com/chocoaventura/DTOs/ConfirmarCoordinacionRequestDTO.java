package com.chocoaventura.DTOs;

import java.util.List;

import lombok.Data;

@Data
public class ConfirmarCoordinacionRequestDTO {
    // Flujo original: confirmación del dueño para abrir la coordinación grupal.
    private Long duenoId;
    private Boolean confirmar;

    // Flujo Flutter actual: el usuario termina su exploración individual y envía sus planes elegidos.
    private Long usuarioId;
    private List<String> actividadesInteresIds;
}
