package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UsuarioRequestDTO {
    private String nombre;
    private String correo;
    private String contrasena;
    private String telefono;
}