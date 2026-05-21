package com.chocoaventura.DTOs;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class EstadoExploracionGrupalDTO {
    private Long grupoId;
    private String estadoGrupo;
    private boolean todosLosPerfilesListos;
    private boolean requiereConfirmacionDueno;
    private int totalPerfilesDecisores;
    private int perfilesListos;
    private String mensaje;
    private List<ParticipanteEstadoDTO> participantes;
    private Boolean usuarioActualExploro;
    private Boolean usuarioActualDebeExplorar;
    private Boolean usuarioActualPuedeExplorar;
    private Integer faltanPorExplorar;

    public EstadoExploracionGrupalDTO(
            Long grupoId,
            String estadoGrupo,
            boolean todosLosPerfilesListos,
            boolean requiereConfirmacionDueno,
            int totalPerfilesDecisores,
            int perfilesListos,
            String mensaje) {
        this(grupoId, estadoGrupo, todosLosPerfilesListos, requiereConfirmacionDueno,
                totalPerfilesDecisores, perfilesListos, mensaje, new ArrayList<>());
    }

    public EstadoExploracionGrupalDTO(
            Long grupoId,
            String estadoGrupo,
            boolean todosLosPerfilesListos,
            boolean requiereConfirmacionDueno,
            int totalPerfilesDecisores,
            int perfilesListos,
            String mensaje,
            List<ParticipanteEstadoDTO> participantes) {
        this.grupoId = grupoId;
        this.estadoGrupo = estadoGrupo;
        this.todosLosPerfilesListos = todosLosPerfilesListos;
        this.requiereConfirmacionDueno = requiereConfirmacionDueno;
        this.totalPerfilesDecisores = totalPerfilesDecisores;
        this.perfilesListos = perfilesListos;
        this.mensaje = mensaje;
        this.participantes = participantes == null ? new ArrayList<>() : participantes;
        this.faltanPorExplorar = Math.max(0, totalPerfilesDecisores - perfilesListos);
    }

    public EstadoExploracionGrupalDTO(
            Long grupoId,
            String estadoGrupo,
            boolean todosLosPerfilesListos,
            boolean requiereConfirmacionDueno,
            int totalPerfilesDecisores,
            int perfilesListos,
            String mensaje,
            List<ParticipanteEstadoDTO> participantes,
            Boolean usuarioActualExploro,
            Boolean usuarioActualDebeExplorar,
            Boolean usuarioActualPuedeExplorar) {
        this(grupoId, estadoGrupo, todosLosPerfilesListos, requiereConfirmacionDueno,
                totalPerfilesDecisores, perfilesListos, mensaje, participantes);
        this.usuarioActualExploro = usuarioActualExploro;
        this.usuarioActualDebeExplorar = usuarioActualDebeExplorar;
        this.usuarioActualPuedeExplorar = usuarioActualPuedeExplorar;
    }

    // Alias que consume el front actual.
    public int getTotalParticipantes() {
        return totalPerfilesDecisores;
    }

    public boolean isMesaHabilitada() {
        return todosLosPerfilesListos;
    }

    public int getFaltanPorExploracion() {
        return faltanPorExplorar == null ? Math.max(0, totalPerfilesDecisores - perfilesListos) : faltanPorExplorar;
    }

    public boolean isTodosListos() {
        return todosLosPerfilesListos;
    }
}
