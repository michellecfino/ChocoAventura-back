package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.ItemItinerario;
import com.chocoaventura.services.ItemItinerarioService;

@RestController
@RequestMapping("/items-itinerario")
public class ItemItinerarioController {

    @Autowired
    private ItemItinerarioService itemItinerarioService;

    @PostMapping
    public ResponseEntity<ItemItinerario> create(@RequestBody ItemItinerario itemItinerario) {
        return ResponseEntity.ok(itemItinerarioService.create(itemItinerario));
    }

    @GetMapping
    public List<ItemItinerario> getAll() {
        return itemItinerarioService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ItemItinerario> getById(@PathVariable Long id) {
        return ResponseEntity.ok(itemItinerarioService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ItemItinerario> update(@PathVariable Long id, @RequestBody ItemItinerario itemItinerario) {
        return ResponseEntity.ok(itemItinerarioService.update(id, itemItinerario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        itemItinerarioService.delete(id);
        return ResponseEntity.ok("Item de itinerario eliminado correctamente");
    }
}