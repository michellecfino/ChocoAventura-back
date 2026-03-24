package com.chocoaventura.entities;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "imagenes")
@Getter
@Setter
@NoArgsConstructor
public class Imagen {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la imagen

    @Column(nullable = false)
    private String url; // link de la imagen

    @Column(nullable = false)
    private boolean esPrincipal = false;

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    @JsonIgnore
    private Actividad actividad; // actividad a la que pertenece

    public Imagen(String url, Actividad actividad) {
        this.url = url;
        this.actividad = actividad;
    }

    public Imagen(String url, boolean esPrincipal, Actividad actividad) {
        this.url = url;
        this.esPrincipal = esPrincipal;
        this.actividad = actividad;
    }
}