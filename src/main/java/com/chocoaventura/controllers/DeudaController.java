package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Deuda;
import com.chocoaventura.services.DeudaService;

@RestController
@RequestMapping("/deudas")
public class DeudaController {

    @Autowired
    private DeudaService deudaService;

    @PostMapping
    public ResponseEntity<Deuda> create(@RequestBody Deuda deuda) {
        return ResponseEntity.ok(deudaService.create(deuda));
    }

    @GetMapping
    public List<Deuda> getAll() {
        return deudaService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Deuda> getById(@PathVariable Long id) {
        return ResponseEntity.ok(deudaService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Deuda> update(@PathVariable Long id, @RequestBody Deuda deuda) {
        return ResponseEntity.ok(deudaService.update(id, deuda));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        deudaService.delete(id);
        return ResponseEntity.ok("Deuda eliminada correctamente");
    }
}