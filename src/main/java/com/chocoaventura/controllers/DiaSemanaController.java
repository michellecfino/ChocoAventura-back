package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.DiaSemana;
import com.chocoaventura.services.DiaSemanaService;

@RestController
@RequestMapping("/dias-semana")
public class DiaSemanaController {

    @Autowired
    private DiaSemanaService diaSemanaService;

    @PostMapping
    public ResponseEntity<DiaSemana> create(@RequestBody DiaSemana diaSemana) {
        return ResponseEntity.ok(diaSemanaService.create(diaSemana));
    }

    @GetMapping
    public List<DiaSemana> getAll() {
        return diaSemanaService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<DiaSemana> getById(@PathVariable Long id) {
        return ResponseEntity.ok(diaSemanaService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<DiaSemana> update(@PathVariable Long id, @RequestBody DiaSemana diaSemana) {
        return ResponseEntity.ok(diaSemanaService.update(id, diaSemana));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        diaSemanaService.delete(id);
        return ResponseEntity.ok("DiaSemana eliminado correctamente");
    }
}