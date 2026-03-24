package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "participaciones_pago")
@Getter
@Setter
@NoArgsConstructor
public class ParticipacionPago {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la participación

    @Column(nullable = false)
    private Double montoPagado; // cuánto pagó realmente este perfil

    @Column(nullable = false)
    private Double porcentajeResponsabilidad; // qué porcentaje debía asumir

    @ManyToOne
    @JoinColumn(name = "perfil_id", nullable = false)
    private Perfil perfil; // perfil involucrado en el pago

    @ManyToOne
    @JoinColumn(name = "pago_id", nullable = false)
    private Pago pago; // pago al que pertenece esta participación

    public ParticipacionPago(Double montoPagado, Double porcentajeResponsabilidad, Perfil perfil, Pago pago) {
        this.montoPagado = montoPagado;
        this.porcentajeResponsabilidad = porcentajeResponsabilidad;
        this.perfil = perfil;
        this.pago = pago;
    }
}
