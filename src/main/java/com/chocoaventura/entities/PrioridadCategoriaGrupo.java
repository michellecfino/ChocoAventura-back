package com.chocoaventura.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "prioridades_categoria_grupo")
@Getter
@Setter
@NoArgsConstructor
public class PrioridadCategoriaGrupo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "grupo_viaje_id", nullable = false)
    private GrupoViaje grupoViaje;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "perfil_id")
    private Perfil perfil;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "categoria_id", nullable = false)
    private Categoria categoria;

    @Column(nullable = false)
    private Integer posicion;

    @Column(nullable = false)
    private Integer puntaje;

    @Column(nullable = false)
    private LocalDateTime creadoEn;

    @Column(nullable = false)
    private LocalDateTime actualizadoEn;

    public PrioridadCategoriaGrupo(GrupoViaje grupoViaje, Perfil perfil, Categoria categoria, Integer posicion, Integer puntaje) {
        this.grupoViaje = grupoViaje;
        this.perfil = perfil;
        this.categoria = categoria;
        this.posicion = posicion;
        this.puntaje = puntaje;
    }

    @PrePersist
    public void prePersist() {
        LocalDateTime ahora = LocalDateTime.now();
        this.creadoEn = ahora;
        this.actualizadoEn = ahora;
    }

    @PreUpdate
    public void preUpdate() {
        this.actualizadoEn = LocalDateTime.now();
    }
}
