package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class UbicacionResponseDTO {
    private Long id;
    private String nombre;
    private String direccion;
    private Double latitud;
    private Double longitud;
}