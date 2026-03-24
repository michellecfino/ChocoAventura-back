package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ciudades")
@Getter
@Setter
@NoArgsConstructor
public class Ciudad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la ciudad

    @Column(nullable = false)
    private String nombre; // nombre de la ciudad

    @Column(nullable = false)
    private String pais; // país de la ciudad

    @OneToMany(mappedBy = "ciudad", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Actividad> actividades = new HashSet<>(); // actividades de esta ciudad

    @OneToMany(mappedBy = "ciudadDestino", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GrupoViaje> gruposDestino = new HashSet<>(); // viajes cuyo destino es esta ciudad

    public Ciudad(String nombre, String pais) {
        this.nombre = nombre;
        this.pais = pais;
    }
}