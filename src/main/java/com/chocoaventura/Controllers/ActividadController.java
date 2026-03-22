package com.chocoaventura.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.Services.ActividadService;

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
    public List<Actividad> getActividades() {
        return actividadService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Actividad> getById(@PathVariable Long id) {
        return ResponseEntity.ok(actividadService.getById(id));
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

    @GetMapping("/scrapear")
    public String scrapear() {
        actividadService.scrapearIdartes();
        return "OY EL CODIGO NUEVO 2026";
    }
}