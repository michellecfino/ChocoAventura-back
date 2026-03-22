package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id único del usuario

    @Column(nullable = false)
    private String nombre; // nombre del usuario

    @Column(nullable = false, unique = true)
    private String correo; // correo para iniciar sesión

    @Column(nullable = false)
    private String contrasena; // contraseña de la cuenta

    private String telefono; // teléfono del usuario, opcional por ahora

    @OneToMany(mappedBy = "usuario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Perfil> perfiles = new HashSet<>(); // perfiles del usuario en distintos viajes

    @OneToMany(mappedBy = "dueno", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GrupoViaje> gruposCreados = new HashSet<>(); // viajes creados por este usuario
}