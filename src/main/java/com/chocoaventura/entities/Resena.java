package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "resenas")
@Getter
@Setter
@NoArgsConstructor
public class Resena {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la reseña

    @Column(nullable = false)
    private Integer calificacion; // calificación de 1 a 5

    private String comentario; // comentario del usuario

    @Column(nullable = false)
    private LocalDate fecha; // fecha de la reseña

    @ManyToOne
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil; // perfil que dejó la reseña

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad; // actividad reseñada

    public Resena(Integer calificacion, LocalDate fecha, Perfil perfil, Actividad actividad) {
        this.calificacion = calificacion;
        this.fecha = fecha;
        this.perfil = perfil;
        this.actividad = actividad;
    }
    //se crea la reseña y luego se le permite al usuario añadir su comentario
}