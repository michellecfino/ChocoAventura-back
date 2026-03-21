package com.chocoaventura.entities;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.*;

@Entity
@Table(name = "actividades")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder

public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    private String nombre; // Podrías usarlo como subtitulo

    @Column(columnDefinition = "TEXT")
    private String descripcion;

    private Double costoPorPersona;
    private Integer duracion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Double calificacionPromedio;
    private String imagenUrl;
    private String preciosDetallados;

    private String fuente;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}