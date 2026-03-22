package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "rondas_subasta")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RondaSubasta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la ronda

    @Column(nullable = false)
    private LocalDateTime fechaFin; // fecha límite para votar

    @Column(nullable = false)
    private String estado; // estado de la ronda

    @Column(nullable = false)
    private Integer tokensPorPerfil; // tokens máximos por perfil en esta ronda

    @ManyToOne
    @JoinColumn(name = "grupo_viaje_id", nullable = false)
    private GrupoViaje grupoViaje; // viaje al que pertenece la ronda

    @ManyToMany
    @JoinTable(
            name = "ronda_subasta_actividad",
            joinColumns = @JoinColumn(name = "ronda_subasta_id"),
            inverseJoinColumns = @JoinColumn(name = "actividad_id")
    )
    private Set<Actividad> actividadesSubasta = new HashSet<>(); // pool grupal de actividades

    @OneToMany(mappedBy = "rondaSubasta", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AsignacionTokens> asignacionesTokens = new HashSet<>(); // votos hechos en esta ronda
}