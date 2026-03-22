package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "asignaciones_tokens")
@Getter
@Setter
@NoArgsConstructor
public class AsignacionTokens {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la asignación

    @Column(nullable = false)
    private Integer tokensAsignados; // tokens que dio este perfil

    @ManyToOne
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil; // perfil que asigna los tokens

    @ManyToOne
    @JoinColumn(name = "actividad_id", nullable = false)
    private Actividad actividad; // actividad que recibe los tokens

    @ManyToOne
    @JoinColumn(name = "ronda_subasta_id", nullable = false)
    private RondaSubasta rondaSubasta; // ronda en la que se votó

    public AsignacionTokens(Integer tokensAsignados, Perfil perfil, Actividad actividad, RondaSubasta rondaSubasta) {
        this.tokensAsignados = tokensAsignados;
        this.perfil = perfil;
        this.actividad = actividad;
        this.rondaSubasta = rondaSubasta;
    }
}