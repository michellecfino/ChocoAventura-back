package com.chocoaventura.controllers;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chocoaventura.DTOs.CategoriaResponseDTO;
import com.chocoaventura.entities.Categoria;
import com.chocoaventura.services.CategoriaService;

@RestController
@RequestMapping("/categorias")
public class CategoriaController {

    @Autowired
    private CategoriaService categoriaService;

    @PostMapping
    public ResponseEntity<Categoria> create(@RequestBody Categoria categoria) {
        return ResponseEntity.ok(categoriaService.create(categoria));
    }

    @GetMapping
    public List<CategoriaResponseDTO> getAll() {
        List<CategoriaResponseDTO> respuesta= new ArrayList<>();
        for (Categoria c: categoriaService.getAll()){
            respuesta.add(new CategoriaResponseDTO(c.getId(), c.getNombre(), c.getDescripcion()));
        }
        return respuesta;
    }

    @GetMapping("/{id}")
    public ResponseEntity<CategoriaResponseDTO> getById(@PathVariable Long id) {
        Categoria c = categoriaService.getById(id);
        return ResponseEntity.ok(new CategoriaResponseDTO(c.getId(), c.getNombre(), c.getDescripcion()));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Categoria> update(@PathVariable Long id, @RequestBody Categoria categoria) {
        return ResponseEntity.ok(categoriaService.update(id, categoria));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        categoriaService.delete(id);
        return ResponseEntity.ok("Categoría eliminada correctamente");
    }
}