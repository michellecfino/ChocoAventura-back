package com.chocoaventura.Controllers;

import java.time.LocalDateTime;
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

import com.chocoaventura.Services.ItemItinerarioService;
import com.chocoaventura.entities.ItemItinerario;

import jakarta.persistence.EntityNotFoundException;

@RestController
@RequestMapping("/items-itinerario")
public class ItemItinerarioController {

    @Autowired
    private ItemItinerarioService itemItinerarioService;

    @PostMapping
    public ResponseEntity<ItemItinerario> create(@PathVariable LocalDateTime inicioProgramado,
        @PathVariable LocalDateTime finProgramado, 
        @PathVariable Long itinerarioId, 
        @PathVariable Long actividadId
    ) throws EntityNotFoundException{
        return ResponseEntity.ok(itemItinerarioService.agregarActividadAItinerario(inicioProgramado, finProgramado, itinerarioId, actividadId));
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