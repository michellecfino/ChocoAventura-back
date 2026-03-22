package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalTime;

@Entity
@Table(name = "horarios")
@Getter
@Setter
@NoArgsConstructor
public class Horario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id del horario

    private LocalTime horaInicio; // hora de inicio

    private LocalTime horaFin; // hora de fin

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad; // actividad que usa este horario

    @ManyToOne
    @JoinColumn(name = "dias_semana_id")
    private DiaSemana diasSemana; // días en que aplica este horario

    public Horario(LocalTime horaInicio, LocalTime horaFin, Actividad actividad) {
        this.horaInicio = horaInicio;
        this.horaFin = horaFin;
        this.actividad = actividad;
    }
}