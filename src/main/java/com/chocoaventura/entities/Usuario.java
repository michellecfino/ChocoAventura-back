package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.*;


import java.util.List;

@Entity
@Table(name = "usuarios")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false, unique = true)
    private String login;

    @Column(nullable = false, unique = true)
    private String correo;

    private String telefono;

    

    @OneToMany(mappedBy = "usuario")
    private List<Perfil> perfiles;
}