package com.chocoaventura.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.AsignacionTokens;
import com.chocoaventura.Services.AsignacionTokensService;

@RestController
@RequestMapping("/asignaciones-tokens")
public class AsignacionTokensController {

    @Autowired
    private AsignacionTokensService asignacionTokensService;

    @PostMapping
    public ResponseEntity<AsignacionTokens> create(@RequestBody AsignacionTokens asignacionTokens) {
        return ResponseEntity.ok(asignacionTokensService.create(asignacionTokens));
    }

    @GetMapping
    public List<AsignacionTokens> getAll() {
        return asignacionTokensService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AsignacionTokens> getById(@PathVariable Long id) {
        return ResponseEntity.ok(asignacionTokensService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AsignacionTokens> update(@PathVariable Long id, @RequestBody AsignacionTokens asignacionTokens) {
        return ResponseEntity.ok(asignacionTokensService.update(id, asignacionTokens));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        asignacionTokensService.delete(id);
        return ResponseEntity.ok("Asignación de tokens eliminada correctamente");
    }
}