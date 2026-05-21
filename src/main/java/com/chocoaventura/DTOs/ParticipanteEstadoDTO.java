package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ParticipanteEstadoDTO {
    private Long perfilId;
    private Long usuarioId;
    private String nombre;
    private Boolean listo;
}
