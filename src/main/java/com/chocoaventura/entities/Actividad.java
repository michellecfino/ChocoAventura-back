package com.chocoaventura.entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.Builder;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonIgnore;

@Entity
@Table(name = "actividades")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Actividad {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // id de la actividad

    @Column(nullable = false)
    private String nombre; // nombre de la actividad

    @Column(columnDefinition = "TEXT")
    private String descripcion; // descripción de la actividad
    @Column(columnDefinition = "TEXT")
    private String imagenUrl;

    @Column(nullable = false)
    private Double costoPorPersona; // costo estimado por persona

    @Column(nullable = false)
    private Integer duracionMin; // duración estimada en minutos

    private Double calificacionPromedio; // calificación promedio de 0 a 5

    private LocalDate vigenciaInicio; // desde cuándo aplica, si tiene rango

    private LocalDate vigenciaFin; // hasta cuándo aplica, si tiene rango

    @ElementCollection
    @CollectionTable(name = "actividad_precios", joinColumns = @JoinColumn(name = "actividad_id"))
    @MapKeyColumn(name = "localidad")
    @Column(name = "precio")
    private Map<String, Double> preciosDetallados = new HashMap<>(); // texto con precios por zonas o tipos

    private String fuente; // de dónde salió la actividad
    @Column(columnDefinition = "TEXT")
    private String fuenteUrl; // URL de la fuente
    private String categoriaString; // categoría como string
    private LocalDateTime fechaCreacion; // fecha de creación del registro

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ciudad_id")
    private Ciudad ciudad; // ciudad donde se hace

    @ManyToOne(fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    @JoinColumn(name = "ubicacion_id")
    private Ubicacion ubicacion; // ubicación puntual de la actividad

    @ManyToMany
    @JoinTable(name = "actividad_categoria", joinColumns = @JoinColumn(name = "actividad_id"), inverseJoinColumns = @JoinColumn(name = "categoria_id"))
    private Set<Categoria> categorias = new HashSet<>(); // categorías a las que pertenece

    @OneToMany(fetch = FetchType.EAGER, mappedBy = "actividad", cascade = CascadeType.ALL, orphanRemoval = true)
    @JsonIgnore
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

    public void setPreciosDetallados(Map<String, Double> preciosDetallados) {
        this.preciosDetallados = preciosDetallados;
    }
}