package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.DTOs.ActividadResponseDTO;
import com.chocoaventura.entities.Actividad;
import com.chocoaventura.services.ActividadService;

@RestController
@RequestMapping("/actividades")
public class ActividadController {

    @Autowired
    private ActividadService actividadService;

    @PostMapping
    public ResponseEntity<Actividad> create(@RequestBody Actividad actividad) {
        return ResponseEntity.ok(actividadService.create(actividad));
    }

    @GetMapping
    public List<ActividadResponseDTO> getActividades() {
        return actividadService.getAllDTOs();
    }

    @GetMapping("/scrapear")
    public String scrapear() {
        actividadService.actualizarTodo();

        return "OY EL CODIGO NUEVO 2026";
    }

    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ActividadResponseDTO> getById(@PathVariable Long id) {
        Actividad actividad = actividadService.getById(id);
        ActividadResponseDTO dto = actividadService.mapToDTO(actividad);
        return ResponseEntity.ok(dto);
    }

    @PutMapping("/{id}")
    public ResponseEntity<Actividad> update(@PathVariable Long id, @RequestBody Actividad actividad) {
        return ResponseEntity.ok(actividadService.update(id, actividad));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        actividadService.delete(id);
        return ResponseEntity.ok("Actividad eliminada correctamente");
    }

    //método para borrar todo
    @DeleteMapping("/borrar-todo")
    public ResponseEntity<String> deleteAll() {
        actividadService.deleteAll();
        return ResponseEntity.ok("Todas las actividades eliminadas correctamente");
    }

}