package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "items_itinerario")
@Getter
@Setter
@NoArgsConstructor
public class ItemItinerario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id del bloque del itinerario

    @Column(nullable = false)
    private LocalDateTime inicioProgramado; // cuándo empieza, fecha y hora

    @Column(nullable = false)
    private LocalDateTime finProgramado; // cuándo termina, fecha y hora

    @Column(nullable = false)
    private String estado; // programada, completada, omitida, etc.

    @ManyToOne
    @JoinColumn(name = "itinerario_id", nullable = false)
    private Itinerario itinerario; // itinerario al que pertenece

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad; // actividad que se hará

    public ItemItinerario(LocalDateTime inicioProgramado, LocalDateTime finProgramado, Itinerario itinerario, Actividad actividad) {
        this.inicioProgramado = inicioProgramado;
        this.finProgramado = finProgramado;
        this.estado = "PROGRAMADA";
        this.itinerario = itinerario;
        this.actividad = actividad;
    }
}