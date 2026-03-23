package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ImagenResponseDTO {
    private Long id;
    private String url;
    private Long actividadId;
}