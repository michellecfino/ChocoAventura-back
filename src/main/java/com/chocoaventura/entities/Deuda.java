package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "deudas")
@Getter
@Setter
@NoArgsConstructor
public class Deuda {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la deuda

    @Column(nullable = false)
    private Double monto; // cuánto se debe

    @Column(nullable = false)
    private Boolean saldada = false; // si ya fue pagada

    @ManyToOne
    @JoinColumn(name = "deudor_id", nullable = false)
    private Perfil deudor; // quien debe

    @ManyToOne
    @JoinColumn(name = "acreedor_id", nullable = false)
    private Perfil acreedor; // a quien le deben

    @ManyToOne
    @JoinColumn(name = "pago_origen_id")
    private Pago pagoOrigen; // pago que originó esta deuda

    public Deuda(Double monto, Perfil deudor, Perfil acreedor, Pago pagoOrigen) {
        this.monto = monto;
        this.saldada = false;
        this.deudor = deudor;
        this.acreedor = acreedor;
        this.pagoOrigen = pagoOrigen;
    }
}
