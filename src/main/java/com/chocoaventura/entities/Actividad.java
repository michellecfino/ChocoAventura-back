package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;

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
    private String nombre;

    private String descripcion;

    private Double costoPorPersona;

    private Integer duracion;

    private LocalDate fechaInicio;

    private LocalDate fechaFin;

    private Double calificacionPromedio;

    @ManyToOne
    @JoinColumn(name = "categoria_id")
    private Categoria categoria;
}