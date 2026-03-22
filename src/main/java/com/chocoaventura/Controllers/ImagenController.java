package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Imagen;
import com.chocoaventura.services.ImagenService;

@RestController
@RequestMapping("/imagenes")
public class ImagenController {

    @Autowired
    private ImagenService imagenService;

    @PostMapping
    public ResponseEntity<Imagen> create(@RequestBody Imagen imagen) {
        return ResponseEntity.ok(imagenService.create(imagen));
    }

    @GetMapping
    public List<Imagen> getAll() {
        return imagenService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Imagen> getById(@PathVariable Long id) {
        return ResponseEntity.ok(imagenService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Imagen> update(@PathVariable Long id, @RequestBody Imagen imagen) {
        return ResponseEntity.ok(imagenService.update(id, imagen));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        imagenService.delete(id);
        return ResponseEntity.ok("Imagen eliminada correctamente");
    }
}