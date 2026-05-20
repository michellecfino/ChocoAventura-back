package com.chocoaventura.controllers;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chocoaventura.DTOs.ActividadItinerarioDTO;
import com.chocoaventura.DTOs.DiaItinerarioDTO;
import com.chocoaventura.DTOs.ImagenResponseDTO;
import com.chocoaventura.DTOs.ItemItinerarioResponseDTO;
import com.chocoaventura.DTOs.ItinerarioRequestDTO;
import com.chocoaventura.DTOs.ItinerarioResponseDTO;
import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Imagen;
import com.chocoaventura.entities.ItemItinerario;
import com.chocoaventura.entities.Itinerario;
import com.chocoaventura.services.ItinerarioService;
import com.chocoaventura.repositories.ItinerarioRepository;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/itinerarios")
public class ItinerarioController {

    @Autowired
    private ItinerarioService itinerarioService;

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @PostMapping
    public ItinerarioResponseDTO create(@RequestBody ItinerarioRequestDTO request)
        throws EntityNotFoundException {

    Itinerario itinerario;
    try {
        itinerario = itinerarioService.crearItinerario(
            request.getNombre(),
            request.getGrupoViajeId()
        );
    } catch (IllegalStateException | IllegalArgumentException e) {
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage(), e);
    }
    Map<LocalDate, List<ItemItinerario>> agrupado = new LinkedHashMap<>();

    for (ItemItinerario item : itinerario.getItems()) {
        LocalDate fecha = item.getInicioProgramado().toLocalDate();
        agrupado.computeIfAbsent(fecha, k -> new ArrayList<>()).add(item);
    }

    // 🔹 CONVERTIR A DTO
    List<DiaItinerarioDTO> dias = new ArrayList<>();

    for (Map.Entry<LocalDate, List<ItemItinerario>> entry : agrupado.entrySet()) {

        List<ItemItinerarioResponseDTO> itemsDTO = entry.getValue().stream()
            .map(item -> new ItemItinerarioResponseDTO(
                item.getId(),
                item.getInicioProgramado(),
                item.getFinProgramado(),
                item.getEstado(),
                item.getItinerario().getId(),
                actividadtoDTO(item.getActividad())
            ))
            .toList();

        dias.add(new DiaItinerarioDTO(entry.getKey(), itemsDTO));
    }
    
    return new ItinerarioResponseDTO(
        itinerario.getId(),
        itinerario.getNombre(),
        itinerario.getPresupuestoPromedioPersona(),
        dias
    );
    }

    public static ActividadItinerarioDTO actividadtoDTO(Actividad actividad) {
        if (actividad == null) return null;

        return new ActividadItinerarioDTO(
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
            actividad.getUbicacion() == null ? "" : actividad.getUbicacion().getDireccion(),
            toDTOSet(actividad.getImagenes())
        );
    }

    public static ImagenResponseDTO toDTO(Imagen imagen) {
        if (imagen == null) return null;

        return new ImagenResponseDTO(
            imagen.getId(),
            imagen.getUrl(),
            imagen.getActividad() != null ? imagen.getActividad().getId() : null
        );
    }

    public static Set<ImagenResponseDTO> toDTOSet(Set<Imagen> imagenes) {
    if (imagenes == null) return new HashSet<>();

    Set<ImagenResponseDTO> images= new HashSet<>();
    for (Imagen i: imagenes){
        images.add(toDTO(i));
    }
    return images;
    }


    private ItinerarioResponseDTO toResponseDTO(Itinerario itinerario) {
        Map<LocalDate, List<ItemItinerario>> agrupado = new LinkedHashMap<>();

        for (ItemItinerario item : itinerario.getItems()) {
            LocalDate fecha = item.getInicioProgramado().toLocalDate();
            agrupado.computeIfAbsent(fecha, k -> new ArrayList<>()).add(item);
        }

        List<DiaItinerarioDTO> dias = new ArrayList<>();

        for (Map.Entry<LocalDate, List<ItemItinerario>> entry : agrupado.entrySet()) {
            List<ItemItinerarioResponseDTO> itemsDTO = entry.getValue().stream()
                .map(item -> new ItemItinerarioResponseDTO(
                    item.getId(),
                    item.getInicioProgramado(),
                    item.getFinProgramado(),
                    item.getEstado(),
                    item.getItinerario().getId(),
                    actividadtoDTO(item.getActividad())
                ))
                .toList();
            dias.add(new DiaItinerarioDTO(entry.getKey(), itemsDTO));
        }

        return new ItinerarioResponseDTO(
            itinerario.getId(),
            itinerario.getNombre(),
            itinerario.getPresupuestoPromedioPersona(),
            dias
        );
    }

    @GetMapping
    public ResponseEntity<String> getAll() {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body("Use /itinerarios/usuarios/{usuarioId}. Los itinerarios son información privada del usuario autenticado.");
    }

    @GetMapping("/usuarios/{usuarioId}")
    public List<ItinerarioResponseDTO> getByUsuario(@PathVariable Long usuarioId) {
        return itinerarioRepository.findByUsuarioParticipante(usuarioId)
            .stream()
            .map(this::toResponseDTO)
            .toList();
    }


    @GetMapping("/grupos/{grupoId}/actual")
    public ItinerarioResponseDTO getActualPorGrupo(
            @PathVariable Long grupoId,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {
        Itinerario itinerario = itinerarioService.obtenerItinerarioActualPorGrupo(grupoId);
        if (usuarioId != null && !itinerarioRepository.existsByIdAndUsuarioParticipante(itinerario.getId(), usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El itinerario no pertenece al usuario indicado");
        }
        return toResponseDTO(itinerario);
    }

    @GetMapping("/{id}")
    public ItinerarioResponseDTO getById(
            @PathVariable Long id,
            @RequestParam(name = "usuarioId", required = false) Long usuarioId) {
        if (usuarioId != null && !itinerarioRepository.existsByIdAndUsuarioParticipante(id, usuarioId)) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "El itinerario no pertenece al usuario indicado");
        }
        Itinerario itinerario = itinerarioService.getById(id);
        return toResponseDTO(itinerario);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Itinerario> update(@PathVariable Long id, @RequestBody Itinerario itinerario) {
        return ResponseEntity.ok(itinerarioService.update(id, itinerario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        itinerarioService.delete(id);
        return ResponseEntity.ok("Itinerario eliminado correctamente");
    }
}