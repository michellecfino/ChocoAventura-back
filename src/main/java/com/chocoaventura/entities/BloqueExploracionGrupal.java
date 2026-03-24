package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Entity
@Table(name = "bloques_exploracion_grupal")
@Getter
@Setter
@NoArgsConstructor
public class BloqueExploracionGrupal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String titulo;

    @Column(nullable = false)
    private String familiaPlan;

    @Column(nullable = false)
    private Integer ordenVisual;

    @Column(nullable = false)
    private Double scoreGrupo;

    @Column(nullable = false)
    private Integer cantidadPerfilesRepresentados;

    @ManyToOne
    @JoinColumn(name = "ronda_subasta_id", nullable = false)
    private RondaSubasta rondaSubasta;

    @ManyToOne
    @JoinColumn(name = "actividad_principal_id", nullable = false)
    private Actividad actividadPrincipal;

    @ManyToMany
    @JoinTable(
            name = "bloque_exploracion_actividad_similar",
            joinColumns = @JoinColumn(name = "bloque_id"),
            inverseJoinColumns = @JoinColumn(name = "actividad_id")
    )
    private Set<Actividad> actividadesSimilares = new LinkedHashSet<>();

    public BloqueExploracionGrupal(String titulo, String familiaPlan, Integer ordenVisual, Double scoreGrupo,
                                   Integer cantidadPerfilesRepresentados, RondaSubasta rondaSubasta,
                                   Actividad actividadPrincipal) {
        this.titulo = titulo;
        this.familiaPlan = familiaPlan;
        this.ordenVisual = ordenVisual;
        this.scoreGrupo = scoreGrupo;
        this.cantidadPerfilesRepresentados = cantidadPerfilesRepresentados;
        this.rondaSubasta = rondaSubasta;
        this.actividadPrincipal = actividadPrincipal;
    }
}
