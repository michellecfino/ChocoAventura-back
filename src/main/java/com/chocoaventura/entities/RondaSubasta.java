package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rondas_subasta")
@Getter
@Setter
@NoArgsConstructor
public class RondaSubasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Integer numeroRonda;

    @Column(nullable = false)
    private LocalDate bloqueInicio;

    @Column(nullable = false)
    private LocalDate bloqueFin;

    @Column(nullable = false)
    private LocalDateTime fechaFin;

    @Column(nullable = false)
    private String estado;

    @Column(nullable = false)
    private Integer tokensPorPerfil;

    @ManyToOne
    @JoinColumn(name = "grupo_viaje_id", nullable = false)
    private GrupoViaje grupoViaje;

    @ManyToMany
    @JoinTable(
            name = "ronda_subasta_actividad",
            joinColumns = @JoinColumn(name = "ronda_subasta_id"),
            inverseJoinColumns = @JoinColumn(name = "actividad_id")
    )
    private Set<Actividad> actividadesSubasta = new HashSet<>();

    @OneToMany(mappedBy = "rondaSubasta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AsignacionTokens> asignacionesTokens = new HashSet<>();

    @OneToMany(mappedBy = "rondaSubasta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<BloqueExploracionGrupal> bloquesExploracion = new HashSet<>();

    public RondaSubasta(Integer numeroRonda, LocalDate bloqueInicio, LocalDate bloqueFin, LocalDateTime fechaFin,
                        Integer tokensPorPerfil, GrupoViaje grupoViaje) {
        this.numeroRonda = numeroRonda;
        this.bloqueInicio = bloqueInicio;
        this.bloqueFin = bloqueFin;
        this.fechaFin = fechaFin;
        this.tokensPorPerfil = tokensPorPerfil;
        this.estado = "PENDIENTE";
        this.grupoViaje = grupoViaje;
    }
}
