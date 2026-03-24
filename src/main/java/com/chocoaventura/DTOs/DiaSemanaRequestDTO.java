package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class DiaSemanaRequestDTO {
    private Boolean lunes;
    private Boolean martes;
    private Boolean miercoles;
    private Boolean jueves;
    private Boolean viernes;
    private Boolean sabado;
    private Boolean domingo;
}