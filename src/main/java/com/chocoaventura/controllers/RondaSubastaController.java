package com.chocoaventura.controllers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.RondaSubasta;
import com.chocoaventura.services.RondaSubastaService;

@RestController
@RequestMapping("/rondas-subasta")
@CrossOrigin(origins = "*")
public class RondaSubastaController {

    @Autowired
    private RondaSubastaService rondaSubastaService;

    @PostMapping
    public ResponseEntity<Map<String, Object>> create(@RequestBody RondaSubasta rondaSubasta) {
        return ResponseEntity.ok(toMap(rondaSubastaService.create(rondaSubasta)));
    }

    @GetMapping
    public List<Map<String, Object>> getAll() {
        return rondaSubastaService.getAll().stream().map(RondaSubastaController::toMap).toList();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getById(@PathVariable Long id) {
        return ResponseEntity.ok(toMap(rondaSubastaService.getById(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Map<String, Object>> update(@PathVariable Long id, @RequestBody RondaSubasta rondaSubasta) {
        return ResponseEntity.ok(toMap(rondaSubastaService.update(id, rondaSubasta)));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        rondaSubastaService.delete(id);
        return ResponseEntity.ok("Ronda de subasta eliminada correctamente");
    }

    private static Map<String, Object> toMap(RondaSubasta ronda) {
        Map<String, Object> m = new LinkedHashMap<>();
        if (ronda == null) return m;
        m.put("id", ronda.getId());
        m.put("numeroRonda", ronda.getNumeroRonda());
        m.put("bloqueInicio", ronda.getBloqueInicio());
        m.put("bloqueFin", ronda.getBloqueFin());
        m.put("fechaFin", ronda.getFechaFin());
        m.put("estado", ronda.getEstado());
        m.put("tokensPorPerfil", ronda.getTokensPorPerfil());
        m.put("grupoViajeId", ronda.getGrupoViaje() == null ? null : ronda.getGrupoViaje().getId());
        m.put("actividadesSubastaIds", ronda.getActividadesSubasta() == null ? List.of() :
                ronda.getActividadesSubasta().stream().map(a -> a.getId()).toList());
        return m;
    }
}
