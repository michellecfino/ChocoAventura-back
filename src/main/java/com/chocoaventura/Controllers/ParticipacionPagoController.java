package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.ParticipacionPago;
import com.chocoaventura.services.ParticipacionPagoService;

@RestController
@RequestMapping("/participaciones-pago")
public class ParticipacionPagoController {

    @Autowired
    private ParticipacionPagoService participacionPagoService;

    @PostMapping
    public ResponseEntity<ParticipacionPago> create(@RequestBody ParticipacionPago participacionPago) {
        return ResponseEntity.ok(participacionPagoService.create(participacionPago));
    }

    @GetMapping
    public List<ParticipacionPago> getAll() {
        return participacionPagoService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<ParticipacionPago> getById(@PathVariable Long id) {
        return ResponseEntity.ok(participacionPagoService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ParticipacionPago> update(@PathVariable Long id, @RequestBody ParticipacionPago participacionPago) {
        return ResponseEntity.ok(participacionPagoService.update(id, participacionPago));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        participacionPagoService.delete(id);
        return ResponseEntity.ok("Participación de pago eliminada correctamente");
    }
}