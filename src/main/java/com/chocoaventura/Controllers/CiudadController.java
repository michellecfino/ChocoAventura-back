package com.chocoaventura.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Ciudad;
import com.chocoaventura.Services.CiudadService;

@RestController
@RequestMapping("/ciudades")
public class CiudadController {

    @Autowired
    private CiudadService ciudadService;

    @PostMapping
    public ResponseEntity<Ciudad> create(@RequestBody Ciudad ciudad) {
        return ResponseEntity.ok(ciudadService.create(ciudad));
    }

    @GetMapping
    public List<Ciudad> getAll() {
        return ciudadService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Ciudad> getById(@PathVariable Long id) {
        return ResponseEntity.ok(ciudadService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Ciudad> update(@PathVariable Long id, @RequestBody Ciudad ciudad) {
        return ResponseEntity.ok(ciudadService.update(id, ciudad));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        ciudadService.delete(id);
        return ResponseEntity.ok("Ciudad eliminada correctamente");
    }
}