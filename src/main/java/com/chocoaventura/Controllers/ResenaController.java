package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Resena;
import com.chocoaventura.services.ResenaService;

@RestController
@RequestMapping("/resenas")
public class ResenaController {

    @Autowired
    private ResenaService resenaService;

    @PostMapping
    public ResponseEntity<Resena> create(@RequestBody Resena resena) {
        return ResponseEntity.ok(resenaService.create(resena));
    }

    @GetMapping
    public List<Resena> getAll() {
        return resenaService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Resena> getById(@PathVariable Long id) {
        return ResponseEntity.ok(resenaService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Resena> update(@PathVariable Long id, @RequestBody Resena resena) {
        return ResponseEntity.ok(resenaService.update(id, resena));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        resenaService.delete(id);
        return ResponseEntity.ok("Reseña eliminada correctamente");
    }
}