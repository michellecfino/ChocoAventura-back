package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "grupos_viaje")
@Getter
@Setter
@NoArgsConstructor
public class GrupoViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id del viaje grupal

    @Column(nullable = false)
    private String nombre; // nombre del viaje

    private String descripcion; // descripción corta del viaje

    private LocalTime horaInicioActividades; // hora base para empezar actividades

    private LocalTime horaAlmuerzo; // hora estimada para almorzar

    private Integer duracionAlmuerzoMin; // duración estimada del almuerzo en minutos

    @Column(nullable = false)
    private LocalDateTime fechaHoraLlegada; // llegada al destino

    @Column(nullable = false)
    private LocalDateTime fechaHoraSalida; // salida del destino

    @ManyToOne
    @JoinColumn(name = "dueno_id", nullable = false)
    private Usuario dueno; // usuario que creó el viaje

    @ManyToOne
    @JoinColumn(name = "ciudad_destino_id", nullable = false)
    private Ciudad ciudadDestino; // ciudad principal del viaje

    @ManyToOne
    @JoinColumn(name = "estadia_id")
    private Ubicacion estadia; // lugar donde se hospedan

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Perfil> perfiles = new HashSet<>(); // participantes del viaje

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RondaSubasta> rondasSubasta = new HashSet<>(); // rondas de subasta del viaje

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Itinerario> itinerarios = new HashSet<>(); // posibles itinerarios del viaje

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pago> pagos = new HashSet<>(); // pagos registrados del viaje

    public GrupoViaje(String nombre, String descripcion, LocalDateTime fechaHoraLlegada, LocalDateTime fechaHoraSalida, Ciudad ciudadDestino, LocalTime horaAlmuerzo, LocalTime horaInicioActividades, Integer duracionAlmuerzoMin) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaHoraLlegada = fechaHoraLlegada;
        this.fechaHoraSalida = fechaHoraSalida;
        this.ciudadDestino = ciudadDestino;
        this.horaAlmuerzo = horaAlmuerzo;
        this.horaInicioActividades = horaInicioActividades;
        this.duracionAlmuerzoMin = duracionAlmuerzoMin;
    }
}