package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "ubicaciones")
@Getter
@Setter
@NoArgsConstructor
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la ubicación

    private String nombre; // nombre del lugar

    private String direccion; // dirección del lugar

    private Double latitud; // latitud del lugar

    private Double longitud; // longitud del lugar

    @OneToMany(mappedBy = "ubicacion", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Actividad> actividades = new HashSet<>(); // actividades que pasan aquí

    @OneToMany(mappedBy = "estadia", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<GrupoViaje> gruposQueSeHospedan = new HashSet<>(); // viajes que se hospedan aquí

    public Ubicacion(String nombre, String direccion, Double latitud, Double longitud) {
        this.nombre = nombre;
        this.direccion = direccion;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}