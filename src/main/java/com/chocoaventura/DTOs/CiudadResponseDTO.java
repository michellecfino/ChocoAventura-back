package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class CiudadResponseDTO {
    private Long id;
    private String nombre;
    private String pais;
}