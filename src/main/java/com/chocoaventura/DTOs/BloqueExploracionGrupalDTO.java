package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class BloqueExploracionGrupalDTO {
    private Long id;
    private String titulo;
    private String familiaPlan;
    private Double scoreGrupo;
    private Integer cantidadPerfilesRepresentados;
    private ActividadExploracionDTO principal;
    private List<ActividadExploracionDTO> similares;
}
