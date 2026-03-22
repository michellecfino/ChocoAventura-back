package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "dias_semana")
@Getter
@Setter
@NoArgsConstructor
public class DiaSemana {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id del grupo de días

    private Boolean lunes; // si aplica lunes
    private Boolean martes; // si aplica martes
    private Boolean miercoles; // si aplica miércoles
    private Boolean jueves; // si aplica jueves
    private Boolean viernes; // si aplica viernes
    private Boolean sabado; // si aplica sábado
    private Boolean domingo; // si aplica domingo

    @OneToMany(mappedBy = "diasSemana", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Horario> horarios = new HashSet<>(); // horarios que usan este patrón

    public DiaSemana(Boolean lunes, Boolean martes, Boolean miercoles, Boolean jueves, Boolean viernes, Boolean sabado, Boolean domingo) {
        this.lunes = lunes;
        this.martes = martes;
        this.miercoles = miercoles;
        this.jueves = jueves;
        this.viernes = viernes;
        this.sabado = sabado;
        this.domingo = domingo;
    }
}