package com.chocoaventura.entities;

import com.chocoaventura.entities.enums.EstadoGrupoViaje;
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
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private LocalTime horaInicioActividades;

    private LocalTime horaAlmuerzo;

    private Integer duracionAlmuerzoMin;

    @Column(nullable = false)
    private LocalDateTime fechaHoraLlegada;

    @Column(nullable = false)
    private LocalDateTime fechaHoraSalida;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoGrupoViaje estado = EstadoGrupoViaje.ABIERTO;

    private LocalDateTime fechaConfirmacionCoordinacion;

    @ManyToOne
    @JoinColumn(name = "dueno_id", nullable = false)
    private Usuario dueno;

    @ManyToOne
    @JoinColumn(name = "ciudad_destino_id", nullable = false)
    private Ciudad ciudadDestino;

    @ManyToOne
    @JoinColumn(name = "estadia_id")
    private Ubicacion estadia;

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Perfil> perfiles = new HashSet<>();

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<RondaSubasta> rondasSubasta = new HashSet<>();

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Itinerario> itinerarios = new HashSet<>();

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Pago> pagos = new HashSet<>();

    public GrupoViaje(String nombre, String descripcion, LocalTime horaInicioActividades, LocalTime horaAlmuerzo, Integer duracionAlmuerzoMin, LocalDateTime fechaHoraLlegada, LocalDateTime fechaHoraSalida, Ciudad ciudadDestino, Usuario dueno) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.horaInicioActividades = horaInicioActividades;
        this.horaAlmuerzo = horaAlmuerzo;
        this.duracionAlmuerzoMin = duracionAlmuerzoMin;
        this.fechaHoraLlegada = fechaHoraLlegada;
        this.fechaHoraSalida = fechaHoraSalida;
        this.ciudadDestino = ciudadDestino;
        this.dueno = dueno;
        this.estado = EstadoGrupoViaje.ABIERTO;
    }
}
