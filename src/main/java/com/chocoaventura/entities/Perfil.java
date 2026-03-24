package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "perfiles")
@Getter
@Setter
@NoArgsConstructor
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Double presupuesto;

    @Column(nullable = false)
    private Integer personasCargo;

    @Column(nullable = false)
    private Integer tiempoDiarioActividades;

    @Column(nullable = false)
    private Boolean faseIndividualLista = false;

    @Column(nullable = false)
    private Boolean participaEnCoordinacion = true;

    private String codigoVuelo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario;

    @ManyToOne
    @JoinColumn(name = "grupo_viaje_id", nullable = false)
    private GrupoViaje grupoViaje;

    @ManyToMany
    @JoinTable(
            name = "perfil_categoria_preferida",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categoriasPreferidas = new HashSet<>();

    @ManyToMany
    @JoinTable(
            name = "perfil_actividad_seleccionada",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "actividad_id")
    )
    private Set<Actividad> actividadesSeleccionadas = new HashSet<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ParticipacionPago> participacionesPago = new HashSet<>();

    @OneToMany(mappedBy = "deudor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Deuda> deudasComoDeudor = new HashSet<>();

    @OneToMany(mappedBy = "acreedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Deuda> deudasComoAcreedor = new HashSet<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AsignacionTokens> asignacionesTokens = new HashSet<>();

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Resena> resenas = new HashSet<>();

    public Perfil(Double presupuesto, Integer personasCargo, Integer tiempoDiarioActividades, Set<Categoria> categoriasPreferidas) {
        this.presupuesto = presupuesto;
        this.personasCargo = personasCargo;
        this.tiempoDiarioActividades = tiempoDiarioActividades;
        this.categoriasPreferidas = categoriasPreferidas;
        this.faseIndividualLista = false;
        this.participaEnCoordinacion = true;
    }
}
