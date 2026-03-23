package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Perfil;
import com.chocoaventura.services.PerfilService;

@RestController
@RequestMapping("/perfiles")
public class PerfilController {

    @Autowired
    private PerfilService perfilService;

    @PostMapping
    public ResponseEntity<Perfil> create(@RequestBody Perfil perfil) {
        return ResponseEntity.ok(perfilService.create(perfil));
    }

    @GetMapping
    public List<Perfil> getAll() {
        return perfilService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Perfil> getById(@PathVariable Long id) {
        return ResponseEntity.ok(perfilService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Perfil> update(@PathVariable Long id, @RequestBody Perfil perfil) {
        return ResponseEntity.ok(perfilService.update(id, perfil));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        perfilService.delete(id);
        return ResponseEntity.ok("Perfil eliminado correctamente");
    }
}