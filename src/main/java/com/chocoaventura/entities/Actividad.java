package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "actividades")
@Getter
@Setter
@NoArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la actividad

    @Column(nullable = false)
    private String nombre; // nombre de la actividad

    private String descripcion; // descripción de la actividad

    @Column(nullable = false)
    private Double costoPorPersona; // costo estimado por persona

    @Column(nullable = false)
    private Integer duracionMin; // duración estimada en minutos

    private Double calificacionPromedio; // calificación promedio de 0 a 5

    private LocalDate vigenciaInicio; // desde cuándo aplica, si tiene rango

    private LocalDate vigenciaFin; // hasta cuándo aplica, si tiene rango

    @Column(columnDefinition = "TEXT")
    private String preciosDetallados; // texto con precios por zonas o tipos

    private String fuente; // de dónde salió la actividad

    @ManyToOne
    @JoinColumn(name = "ciudad_id")
    private Ciudad ciudad; // ciudad donde se hace

    @ManyToOne
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion; // ubicación puntual de la actividad

    @ManyToMany
    @JoinTable(
            name = "actividad_categoria",
            joinColumns = @JoinColumn(name = "actividad_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>(); // categorías a las que pertenece

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Imagen> imagenes = new HashSet<>(); // imágenes de la actividad

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Horario> horarios = new HashSet<>(); // horarios disponibles

    @ManyToMany(mappedBy = "actividadesSeleccionadas")
    private Set<Perfil> perfilesQueLaSeleccionaron = new HashSet<>(); // perfiles que la marcaron

    @ManyToMany(mappedBy = "actividadesSubasta")
    private Set<RondaSubasta> rondasSubasta = new HashSet<>(); // rondas donde aparece

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Resena> resenas = new HashSet<>(); // reseñas de la actividad

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<ItemItinerario> itemsItinerario = new HashSet<>(); // veces que aparece en itinerarios

    @OneToMany(mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<AsignacionTokens> asignacionesTokens = new HashSet<>(); // tokens recibidos

    public Actividad(String nombre, String descripcion, Double costoPorPersona, Integer duracionMin) {
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.costoPorPersona = costoPorPersona;
        this.duracionMin = duracionMin;
    }
}