package com.chocoaventura.controllers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.AsignacionTokens;
import com.chocoaventura.services.AsignacionTokensService;

@RestController
@RequestMapping("/asignaciones-tokens")
@CrossOrigin(origins = "*")
public class AsignacionTokensController {

    @Autowired
    private AsignacionTokensService asignacionTokensService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody AsignacionTokens asignacionTokens) {
        return ResponseEntity.ok(toMap(asignacionTokensService.create(asignacionTokens)));
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return asignacionTokensService.getAll().stream().map(AsignacionTokensController::toMap).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toMap(asignacionTokensService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody AsignacionTokens asignacionTokens) {
        return ResponseEntity.ok(toMap(asignacionTokensService.update(id, asignacionTokens)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        asignacionTokensService.delete(id);
        return ResponseEntity.ok("Asignación de tokens eliminada correctamente");
    }

    private static Map<String, Object> toMap(AsignacionTokens asignacion) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (asignacion == null) return m;
        m.put("id", asignacion.getId());
        m.put("tokensAsignados", asignacion.getTokensAsignados());
        m.put("perfilId", asignacion.getPerfil() == null ? null : asignacion.getPerfil().getId());
        m.put("actividadId", asignacion.getActividad() == null ? null : asignacion.getActividad().getId());
        m.put("rondaSubastaId", asignacion.getRondaSubasta() == null ? null : asignacion.getRondaSubasta().getId());
        return m;
    }
}
