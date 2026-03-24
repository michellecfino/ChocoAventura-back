package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class RondaExploracionGrupalDTO {
    private Long id;
    private Integer numeroRonda;
    private LocalDate bloqueInicio;
    private LocalDate bloqueFin;
    private Integer tokensPorPerfil;
    private List<BloqueExploracionGrupalDTO> bloques;
}
