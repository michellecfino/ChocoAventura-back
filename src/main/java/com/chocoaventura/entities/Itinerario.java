package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "itinerarios")
@Getter
@Setter
@NoArgsConstructor
public class Itinerario {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id del itinerario

    @Column(nullable = false)
    private String nombre; // nombre del itinerario

    @Column(nullable = false)
    private Double presupuestoPromedioPersona; // costo promedio por persona

    @ManyToOne
    @JoinColumn(name = "grupo_viaje_id", nullable = false)
    private GrupoViaje grupoViaje; // viaje al que pertenece el itinerario

    @OneToMany(mappedBy = "itinerario", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ItemItinerario> items = new HashSet<>(); // actividades programadas

    public Itinerario(String nombre, Double presupuestoPromedioPersona, GrupoViaje grupoViaje) {
        this.nombre = nombre;
        this.presupuestoPromedioPersona = presupuestoPromedioPersona;
        this.grupoViaje = grupoViaje;
    }
}