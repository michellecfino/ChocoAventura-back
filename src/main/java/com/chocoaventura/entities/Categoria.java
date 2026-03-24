package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categorias")
@Getter
@Setter
@NoArgsConstructor
public class Categoria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la categoría

    @Column(nullable = false, unique = true)
    private String nombre; // nombre de la categoría

    private String descripcion; // explicación corta

    @ManyToMany(mappedBy = "categoriasPreferidas")
    private Set<Perfil> perfilesQueLaPrefieren = new HashSet<>(); // perfiles que prefieren esta categoría

    @ManyToMany(mappedBy = "categorias")
    private Set<Actividad> actividades = new HashSet<>(); // actividades con esta categoría

    public Categoria(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }
}