package com.chocoaventura.controllers;

import java.util.List;

import com.chocoaventura.DTOs.ConfirmarCoordinacionRequestDTO;
import com.chocoaventura.DTOs.CrearGrupoDTO;
import com.chocoaventura.DTOs.EstadoExploracionGrupalDTO;
import com.chocoaventura.DTOs.ExploracionGrupalResponseDTO;
import com.chocoaventura.DTOs.UnirseGrupoDTO;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.services.ExploracionGrupalService;
import com.chocoaventura.services.GrupoViajeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos")
public class GrupoViajeController {

    @Autowired
    private GrupoViajeService grupoViajeService;

    @Autowired
    private ExploracionGrupalService exploracionGrupalService;

    @PostMapping
    public ResponseEntity<GrupoViaje> create(@RequestBody GrupoViaje grupoViaje) {
        return ResponseEntity.ok(grupoViajeService.create(grupoViaje));
    }

    @GetMapping
    public List<GrupoViaje> getAll() {
        return grupoViajeService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoViaje> getById(@PathVariable Long id) {
        return ResponseEntity.ok(grupoViajeService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GrupoViaje> update(@PathVariable Long id, @RequestBody GrupoViaje grupoViaje) {
        return ResponseEntity.ok(grupoViajeService.update(id, grupoViaje));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        grupoViajeService.delete(id);
        return ResponseEntity.ok("Grupo eliminado correctamente");
    }

    // Crear grupo
    @PostMapping("/crear")
    public ResponseEntity<GrupoViaje> crearGrupo(@RequestBody CrearGrupoDTO dto) {
        GrupoViaje grupo = grupoViajeService.crearGrupoViaje(dto.getNombre(), dto.getNombreDestino(), dto.getPaisDestino(), dto.getDireccion(), dto.getLat(), dto.getLongi(), dto.getFechaInicio(), dto.getFechaFin(), dto.getDescripcion(), dto.getHoraAlmuerzo(), dto.getHoraInicioActividades(), dto.getTiempoParaAlmorzar(), dto.getDuenoId());
        return ResponseEntity.ok(grupo);
    }

    // Unirse a grupo
    @PostMapping("/unirse")
    public ResponseEntity<String> unirse(@RequestBody UnirseGrupoDTO dto) {
        grupoViajeService.unirseAGrupoViaje(dto);
        return ResponseEntity.ok("Usuario unido correctamente");
    }

    // Generar link de invitación
    @GetMapping("/{grupoId}/invitacion")
    public ResponseEntity<String> generarLink(@PathVariable Long grupoId) {
        String link = grupoViajeService.generarLinkInvitacion(grupoId);
        return ResponseEntity.ok(link);
    }

    @GetMapping("/{grupoId}/exploracion-grupal/estado")
    public ResponseEntity<EstadoExploracionGrupalDTO> obtenerEstadoExploracionGrupal(@PathVariable Long grupoId) {
        return ResponseEntity.ok(exploracionGrupalService.evaluarEstado(grupoId));
    }

    @PostMapping("/{grupoId}/exploracion-grupal/confirmar")
    public ResponseEntity<EstadoExploracionGrupalDTO> confirmarInicioCoordinacion(
            @PathVariable Long grupoId,
            @RequestBody ConfirmarCoordinacionRequestDTO dto
    ) {
        return ResponseEntity.ok(
                exploracionGrupalService.confirmarInicioCoordinacion(grupoId, dto.getDuenoId(), Boolean.TRUE.equals(dto.getConfirmar()))
        );
    }

    @GetMapping("/{grupoId}/exploracion-grupal")
    public ResponseEntity<ExploracionGrupalResponseDTO> obtenerExploracionGrupal(@PathVariable Long grupoId) {
        return ResponseEntity.ok(exploracionGrupalService.obtenerExploracionGrupal(grupoId));
    }
}