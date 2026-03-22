package com.chocoaventura.controllers;

import com.chocoaventura.DTOs.CrearGrupoDTO;
import com.chocoaventura.DTOs.UnirseGrupoDTO;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.services.GrupoViajeService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/grupos")
public class GrupoViajeController {

    @Autowired
    private GrupoViajeService grupoViajeService;

    // Crear grupo
    @PostMapping("/crear")
    public ResponseEntity<GrupoViaje> crearGrupo(@RequestBody CrearGrupoDTO dto) {

        GrupoViaje grupo = grupoViajeService.crearGrupoViaje(
            dto.getNombre(),
            dto.getNombreDestino(),
            dto.getPaisDestino(),
            dto.getDireccion(),
            dto.getLat(),
            dto.getLongi(),
            dto.getFechaInicio(),
            dto.getFechaFin(),
            dto.getDescripcion(),
            dto.getHoraAlmuerzo(),
            dto.getHoraInicioActividades(),
            dto.getTiempoParaAlmorzar(),
            dto.getDuenoId()
        );

        return ResponseEntity.ok(grupo);
    }

    //  Unirse a grupo
    @PostMapping("/unirse")
    public ResponseEntity<String> unirse(@RequestBody UnirseGrupoDTO dto) {

        grupoViajeService.unirseAGrupoViaje(dto);

        return ResponseEntity.ok("Usuario unido correctamente");
    }

    //  Generar link de invitación
    @GetMapping("/{grupoId}/invitacion")
    public ResponseEntity<String> generarLink(@PathVariable Long grupoId) {

        String link = grupoViajeService.generarLinkInvitacion(grupoId);

        return ResponseEntity.ok(link);
    }
}