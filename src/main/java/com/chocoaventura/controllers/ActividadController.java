package com.chocoaventura.controllers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.DTOs.ActividadResponseDTO;
import com.chocoaventura.DTOs.CategoriaResponseDTO;
import com.chocoaventura.DTOs.CiudadResponseDTO;
import com.chocoaventura.DTOs.ImagenResponseDTO;
import com.chocoaventura.DTOs.UbicacionResponseDTO;
import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Categoria;
import com.chocoaventura.entities.Imagen;
import com.chocoaventura.services.ActividadService;

@RestController
@RequestMapping("/actividades")
@CrossOrigin(origins = "*")
public class ActividadController {

    @Autowired
    private ActividadService actividadService;

    @PostMapping
    public ResponseEntity<ActividadResponseDTO> create(@RequestBody Actividad actividad) {
        return ResponseEntity.ok(toResponseDTO(actividadService.create(actividad)));
    }

    @GetMapping("/si")
    public List<ActividadResponseDTO> getActividades() {
        return actividadService.getAll().stream()
                .map(ActividadController::toResponseDTO)
                .toList();
    }

    @GetMapping("/swipe")
    public List<ActividadResponseDTO> getActividadesParaSwipe(
            @RequestParam(required = false) String destinoKey,
            @RequestParam(required = false) Long grupoViajeId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) List<Long> categoriaIds,
            @RequestParam(required = false) List<String> categorias,
            @RequestParam(defaultValue = "false") boolean usarPreferenciasPerfil
    ) {
        return actividadService.getActividadesParaSwipe(destinoKey, grupoViajeId, usuarioId, categoriaIds, categorias, usarPreferenciasPerfil)
                .stream()
                .map(ActividadController::toResponseDTO)
                .toList();
    }

    @GetMapping("/scrapear")
    public String scrapear() {
        actividadService.actualizarTodo();
        return "Scraping iniciado";
    }

    @GetMapping("/scrapear/tuboleta")
    public String scrapearTuboleta() {
        actividadService.scrapearTuBoleta();
        return "Scraping de Tuboleta iniciado. Revisa la consola.";
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ActividadResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toResponseDTO(actividadService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ActividadResponseDTO> update(@PathVariable Long id, @RequestBody Actividad actividad) {
        return ResponseEntity.ok(toResponseDTO(actividadService.update(id, actividad)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        actividadService.delete(id);
        return ResponseEntity.ok("Actividad eliminada correctamente");
    }

    @DeleteMapping("/borrar-todo")
    public ResponseEntity<String> deleteAll() {
        actividadService.deleteAll();
        return ResponseEntity.ok("Todas las actividades eliminadas correctamente");
    }

    private static ActividadResponseDTO toResponseDTO(Actividad actividad) {
        if (actividad == null) return null;

        CiudadResponseDTO ciudad = actividad.getCiudad() == null ? null : new CiudadResponseDTO(
                actividad.getCiudad().getId(),
                actividad.getCiudad().getNombre(),
                actividad.getCiudad().getPais()
        );

        UbicacionResponseDTO ubicacion = actividad.getUbicacion() == null ? null : new UbicacionResponseDTO(
                actividad.getUbicacion().getId(),
                actividad.getUbicacion().getNombre(),
                actividad.getUbicacion().getDireccion(),
                actividad.getUbicacion().getLatitud(),
                actividad.getUbicacion().getLongitud()
        );

        Set<CategoriaResponseDTO> categorias = actividad.getCategorias() == null ? new HashSet<>() :
                actividad.getCategorias().stream()
                        .map(ActividadController::toCategoriaDTO)
                        .collect(Collectors.toCollection(HashSet::new));

        Set<ImagenResponseDTO> imagenes = actividad.getImagenes() == null ? new HashSet<>() :
                actividad.getImagenes().stream()
                        .map(ActividadController::toImagenDTO)
                        .collect(Collectors.toCollection(HashSet::new));

        return new ActividadResponseDTO(
                actividad.getId(),
                actividad.getNombre(),
                actividad.getDescripcion(),
                actividad.getCostoPorPersona(),
                actividad.getDuracionMin(),
                actividad.getCalificacionPromedio(),
                actividad.getVigenciaInicio(),
                actividad.getVigenciaFin(),
                actividad.getPreciosDetallados() == null ? "{}" : actividad.getPreciosDetallados().toString(),
                actividad.getFuente(),
                ciudad,
                ubicacion,
                categorias,
                imagenes
        );
    }

    private static CategoriaResponseDTO toCategoriaDTO(Categoria categoria) {
        return new CategoriaResponseDTO(categoria.getId(), categoria.getNombre(), categoria.getDescripcion());
    }

    private static ImagenResponseDTO toImagenDTO(Imagen imagen) {
        return new ImagenResponseDTO(
                imagen.getId(),
                imagen.getUrl(),
                imagen.getActividad() == null ? null : imagen.getActividad().getId()
        );
    }
}
