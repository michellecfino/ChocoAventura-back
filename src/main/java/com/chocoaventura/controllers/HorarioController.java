package com.chocoaventura.Controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Horario;
import com.chocoaventura.Services.HorarioService;

@RestController
@RequestMapping("/horarios")
public class HorarioController {

    @Autowired
    private HorarioService horarioService;

    @PostMapping
    public ResponseEntity<Horario> create(@RequestBody Horario horario) {
        return ResponseEntity.ok(horarioService.create(horario));
    }

    @GetMapping
    public List<Horario> getAll() {
        return horarioService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Horario> getById(@PathVariable Long id) {
        return ResponseEntity.ok(horarioService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Horario> update(@PathVariable Long id, @RequestBody Horario horario) {
        return ResponseEntity.ok(horarioService.update(id, horario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        horarioService.delete(id);
        return ResponseEntity.ok("Horario eliminado correctamente");
    }
}