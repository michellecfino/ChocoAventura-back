package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaRankingItemDTO {
    private Long categoriaId;
    private String nombre;
    private Integer posicion;
    private Integer puntaje;
}
