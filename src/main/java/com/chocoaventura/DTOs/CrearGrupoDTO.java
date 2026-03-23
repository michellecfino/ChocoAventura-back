package com.chocoaventura.DTOs;

import lombok.Data;

import java.time.LocalDateTime;
import java.time.LocalTime;


@Data
public class CrearGrupoDTO {

    private String nombre;
    private String descripcion;

    private String nombreDestino;
    private String paisDestino;
    private String direccion;

    private double lat;
    private double longi;

    private LocalDateTime fechaInicio;
    private LocalDateTime fechaFin;

    private LocalTime horaAlmuerzo;
    private LocalTime horaInicioActividades;
    private Integer tiempoParaAlmorzar;

    private Long duenoId;

    public CrearGrupoDTO(String nombre, String descripcion, String nombreDestino, String paisDestino, String direccion,
            double lat, double longi, LocalDateTime fechaInicio, LocalDateTime fechaFin, LocalTime horaAlmuerzo,
            LocalTime horaInicioActividades, Integer tiempoParaAlmorzar) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.nombreDestino = nombreDestino;
        this.paisDestino = paisDestino;
        this.direccion = direccion;
        this.lat = lat;
        this.longi = longi;
        this.fechaInicio = fechaInicio;
        this.fechaFin = fechaFin;
        this.horaAlmuerzo = horaAlmuerzo;
        this.horaInicioActividades = horaInicioActividades;
        this.tiempoParaAlmorzar = tiempoParaAlmorzar;
    }

    
}