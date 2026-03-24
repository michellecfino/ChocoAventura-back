package com.chocoaventura.DTOs;

import lombok.Data;

@Data
public class ConfirmarCoordinacionRequestDTO {
    private Long duenoId;
    private Boolean confirmar;
}
