package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImagenRequestDTO {
    private String url;
    private Long actividadId;
}