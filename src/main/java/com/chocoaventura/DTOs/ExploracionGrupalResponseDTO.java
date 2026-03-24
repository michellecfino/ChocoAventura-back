package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class ExploracionGrupalResponseDTO {
    private Long grupoId;
    private String tituloSeccion;
    private String subtituloSeccion;
    private String estadoGrupo;
    private Integer participantesDecisores;
    private Integer participantesRepresentados;
    private List<RondaExploracionGrupalDTO> rondas;
}
