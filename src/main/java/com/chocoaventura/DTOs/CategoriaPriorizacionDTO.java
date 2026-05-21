package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaPriorizacionDTO {
    private Long categoriaId;
    private String nombre;
    private String descripcion;
    private Integer cantidadActividadesSeleccionadas;
    private Integer posicionActual;
    private Integer puntajeActual;
}
