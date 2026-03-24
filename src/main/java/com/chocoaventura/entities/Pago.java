package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "pagos")
@Getter
@Setter
@NoArgsConstructor
public class Pago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id del pago

    @Column(nullable = false)
    private String nombre; // concepto del gasto

    @Column(nullable = false)
    private Double montoTotal; // valor total pagado

    @Column(nullable = false)
    private LocalDate fecha; // fecha del pago

    @ManyToOne
    @JoinColumn(name = "grupo_viaje_id", nullable = false)
    private GrupoViaje grupoViaje; // viaje donde ocurrió el gasto

    @OneToMany(mappedBy = "pago", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ParticipacionPago> participaciones = new HashSet<>(); // cómo se repartió el pago

    @OneToMany(mappedBy = "pagoOrigen", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Deuda> deudasGeneradas = new HashSet<>(); // deudas que salió de este pago

    public Pago(String nombre, Double montoTotal, LocalDate fecha, GrupoViaje grupoViaje) {
        this.nombre = nombre;
        this.montoTotal = montoTotal;
        this.fecha = fecha;
        this.grupoViaje = grupoViaje;
    }
}