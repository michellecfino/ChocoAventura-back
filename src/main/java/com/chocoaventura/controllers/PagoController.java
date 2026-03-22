package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Pago;
import com.chocoaventura.services.PagoService;

@RestController
@RequestMapping("/pagos")
public class PagoController {

    @Autowired
    private PagoService pagoService;

    @PostMapping
    public ResponseEntity<Pago> create(@RequestBody Pago pago) {
        return ResponseEntity.ok(pagoService.create(pago));
    }

    @GetMapping
    public List<Pago> getAll() {
        return pagoService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pago> getById(@PathVariable Long id) {
        return ResponseEntity.ok(pagoService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Pago> update(@PathVariable Long id, @RequestBody Pago pago) {
        return ResponseEntity.ok(pagoService.update(id, pago));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        pagoService.delete(id);
        return ResponseEntity.ok("Pago eliminado correctamente");
    }
}