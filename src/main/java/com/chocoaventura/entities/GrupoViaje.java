package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "grupos_viaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private LocalTime horaAlmuerzo;

    private LocalTime horaInicioActividades;

    private String estadia;

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Perfil> perfiles = new HashSet<>();
}