package com.chocoaventura.entities;

import java.util.List;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "perfiles")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Perfil {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Double presupuesto;

    private Integer personasCargo;

    private Integer tiempo;

    @OneToMany(mappedBy = "perfil")
    private List<Categoria> categoriasPreferidas;

    @OneToMany(mappedBy = "perfil")
    private Usuario usuario;

    @OneToMany
    private GrupoViaje grupoViaje;

    public Perfil(Double presupuesto, Integer personasCargo, Integer tiempo, List<Categoria> categoriasPreferidas) {
        this.presupuesto = presupuesto;
        this.personasCargo = personasCargo;
        this.tiempo = tiempo;
        this.categoriasPreferidas = categoriasPreferidas;
    }

    
}