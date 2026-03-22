package com.chocoaventura.entities;

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

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad; // actividad a la que pertenece

    public Imagen(String url, Actividad actividad) {
        this.url = url;
        this.actividad = actividad;
    }
}