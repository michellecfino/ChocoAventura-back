package com.chocoaventura.entities;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;

import lombok.Getter;

import lombok.Setter;

@Entity
@Table(name = "ciudades")
@Getter
@Setter

public class Ciudad {

    @Id
    @GeneratedValue
    private Long id;
    private String nombre;
    private String pais;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion;

    public Ciudad(String nombre, String pais, Ubicacion ubicacion) {
        this.nombre = nombre;
        this.pais = pais;
        this.ubicacion = ubicacion;
    }

    
}
