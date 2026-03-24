package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/si")
    public List<Actividad> getActividades() {
        return actividadService.getAll();
    }

    @GetMapping("/scrapear")
    public String scrapear() {
        actividadService.actualizarTodo();

        return "OY EL CODIGO NUEVO 2026";
    }

    @GetMapping("/scrapear/tuboleta")
    public String scrapearTuboleta() {
        actividadService.scrapearTuBoleta();
        return "Scraping de Tuboleta iniciado. Revisa la consola.";
    }

    @GetMapping("/{id:[0-9]+}")
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

    //método para borrar todo
    @DeleteMapping("/borrar-todo")
    public ResponseEntity<String> deleteAll() {
        actividadService.deleteAll();
        return ResponseEntity.ok("Todas las actividades eliminadas correctamente");
    }

}