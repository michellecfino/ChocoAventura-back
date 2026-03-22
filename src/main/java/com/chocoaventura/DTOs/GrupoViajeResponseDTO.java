package com.chocoaventura.DTOs;

import java.time.LocalDateTime;
import java.time.LocalTime;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class GrupoViajeResponseDTO {
    private Long id;
    private String nombre;
    private String descripcion;
    private LocalTime horaInicioActividades;
    private LocalTime horaAlmuerzo;
    private Integer duracionAlmuerzoMin;
    private LocalDateTime fechaHoraLlegada;
    private LocalDateTime fechaHoraSalida;
    private CiudadResponseDTO ciudadDestino;
    private UbicacionResponseDTO estadia;
    private UsuarioResponseDTO dueno;
    private Integer cantidadParticipantes;
}