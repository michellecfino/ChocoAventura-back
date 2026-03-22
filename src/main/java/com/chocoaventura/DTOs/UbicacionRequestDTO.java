package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UbicacionRequestDTO {
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
}