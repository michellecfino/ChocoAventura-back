package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chocoaventura.services.ItinerarioService;
import com.chocoaventura.entities.Itinerario;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/itinerarios")
public class ItinerarioController {

    @Autowired
    private ItinerarioService itinerarioService;

    @PostMapping
    public ResponseEntity<Itinerario> create(@PathVariable String nombre, @PathVariable Long grupoViajeId) throws EntityNotFoundException{
        return ResponseEntity.ok(itinerarioService.crearItinerario(nombre, grupoViajeId));
    }

    @GetMapping
    public List<Itinerario> getAll() {
        return itinerarioService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Itinerario> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itinerarioService.getById(id));
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