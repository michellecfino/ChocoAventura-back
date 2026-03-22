package com.chocoaventura.entities;


import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.Getter;

import lombok.Setter;

@Entity
@Table(name = "ciudades")
@Getter
@Setter
public class Ubicacion {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;


    private String direccion;
    private String nombre;
    private double latitud;
    private double longitud;
    public Ubicacion(String direccion, String nombre, double latitud, double longitud) {
        this.direccion = direccion;
        this.nombre = nombre;
        this.latitud = latitud;
        this.longitud = longitud;
    }
}

