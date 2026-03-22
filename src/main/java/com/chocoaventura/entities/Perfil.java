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
    private Long id; // id del perfil dentro del viaje

    @Column(nullable = false)
    private Double presupuesto; // presupuesto para actividades en este viaje

    @Column(nullable = false)
    private Integer personasCargo; // personas que cubre este usuario

    @Column(nullable = false)
    private Integer tiempoDiarioActividades; // tiempo promedio diario para actividades

    @Column(nullable = false)
    private Boolean faseIndividualLista = false; // si ya terminó su exploración individual

    private String codigoVuelo;

    @ManyToOne
    @JoinColumn(name = "usuario_id", nullable = false)
    private Usuario usuario; // usuario dueño de este perfil

    @ManyToOne
    @JoinColumn(name = "grupo_viaje_id", nullable = false)
    private GrupoViaje grupoViaje; // viaje al que pertenece este perfil

    @ManyToMany
    @JoinTable(
            name = "perfil_categoria_preferida",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categoriasPreferidas = new HashSet<>(); // gustos del usuario en este viaje

    @ManyToMany
    @JoinTable(
            name = "perfil_actividad_seleccionada",
            joinColumns = @JoinColumn(name = "perfil_id"),
            inverseJoinColumns = @JoinColumn(name = "actividad_id")
    )
    private Set<Actividad> actividadesSeleccionadas = new HashSet<>(); // actividades que le gustaron

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ParticipacionPago> participacionesPago = new HashSet<>(); // participación en gastos

    @OneToMany(mappedBy = "deudor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Deuda> deudasComoDeudor = new HashSet<>(); // lo que este perfil debe

    @OneToMany(mappedBy = "acreedor", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Deuda> deudasComoAcreedor = new HashSet<>(); // lo que a este perfil le deben

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AsignacionTokens> asignacionesTokens = new HashSet<>(); // votos con tokens en subastas

    @OneToMany(mappedBy = "perfil", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Resena> resenas = new HashSet<>(); // reseñas hechas por este perfil

    public Perfil(Double presupuesto, Integer personasCargo, Integer tiempoDiarioActividades, Set<Categoria> categoriasPreferidas) {
        this.presupuesto = presupuesto;
        this.personasCargo = personasCargo;
        this.tiempoDiarioActividades = tiempoDiarioActividades;
        this.categoriasPreferidas = categoriasPreferidas;
        this.faseIndividualLista = false;
    }

}