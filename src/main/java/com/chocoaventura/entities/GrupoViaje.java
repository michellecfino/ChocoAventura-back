package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.*;

import java.sql.Date;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "grupos_viaje")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GrupoViaje {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    private String descripcion;

    private LocalTime horaAlmuerzo;

    private LocalTime horaInicioActividades;

    private Date fechaInicio;

    private Date fechaFin;

    private String destino;

    private String estadia;

     @OneToMany(mappedBy = "grupo")
    private List<Perfil> perfiles;

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Balance> balances = new HashSet<>();

    @OneToOne(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Subasta subasta;

    @OneToMany(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private Set<Pago> itinerarios = new HashSet<>();

    @OneToOne(mappedBy = "grupoViaje", cascade = CascadeType.ALL, orphanRemoval = true)
    private Itinerario itinerario;

    public GrupoViaje( String nombre, String descripcion, Date fechaInicio, Date fechaFin, String destino) {
   
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.destino = destino;
    }

    

    
}

