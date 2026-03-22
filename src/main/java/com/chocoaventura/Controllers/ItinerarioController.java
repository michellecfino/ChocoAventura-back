package com.chocoaventura.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Itinerario;
import com.chocoaventura.Services.ItinerarioService;

@RestController
@RequestMapping("/itinerarios")
public class ItinerarioController {

    @Autowired
    private ItinerarioService itinerarioService;

    @PostMapping
    public ResponseEntity<Itinerario> create(@RequestBody Itinerario itinerario) {
        return ResponseEntity.ok(itinerarioService.create(itinerario));
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