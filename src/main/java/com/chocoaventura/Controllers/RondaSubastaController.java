package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.RondaSubasta;
import com.chocoaventura.services.RondaSubastaService;

@RestController
@RequestMapping("/rondas-subasta")
public class RondaSubastaController {

    @Autowired
    private RondaSubastaService rondaSubastaService;

    @PostMapping
    public ResponseEntity<RondaSubasta> create(@RequestBody RondaSubasta rondaSubasta) {
        return ResponseEntity.ok(rondaSubastaService.create(rondaSubasta));
    }

    @GetMapping
    public List<RondaSubasta> getAll() {
        return rondaSubastaService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<RondaSubasta> getById(@PathVariable Long id) {
        return ResponseEntity.ok(rondaSubastaService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<RondaSubasta> update(@PathVariable Long id, @RequestBody RondaSubasta rondaSubasta) {
        return ResponseEntity.ok(rondaSubastaService.update(id, rondaSubasta));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        rondaSubastaService.delete(id);
        return ResponseEntity.ok("Ronda de subasta eliminada correctamente");
    }
}