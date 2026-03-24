package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class EstadoExploracionGrupalDTO {
    private Long grupoId;
    private String estadoGrupo;
    private boolean todosLosPerfilesListos;
    private boolean requiereConfirmacionDueno;
    private int totalPerfilesDecisores;
    private int perfilesListos;
    private String mensaje;
}
