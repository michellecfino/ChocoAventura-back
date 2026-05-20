package com.chocoaventura.controllers;

import com.chocoaventura.DTOs.CategoriaPriorizacionRequestDTO;
import com.chocoaventura.DTOs.CategoriaPriorizacionResponseDTO;
import com.chocoaventura.services.PriorizacionCategoriasService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/grupos/{grupoId}/priorizacion-categorias")
public class PriorizacionCategoriasController {

    @Autowired
    private PriorizacionCategoriasService priorizacionCategoriasService;

    @GetMapping
    public CategoriaPriorizacionResponseDTO obtenerOpciones(
            @PathVariable Long grupoId,
            @RequestParam(required = false) Long usuarioId,
            @RequestParam(required = false) Long perfilId
    ) {
        return priorizacionCategoriasService.obtenerOpciones(grupoId, usuarioId, perfilId);
    }

    @PostMapping
    public ResponseEntity<CategoriaPriorizacionResponseDTO> guardarPriorizacion(
            @PathVariable Long grupoId,
            @RequestBody CategoriaPriorizacionRequestDTO request
    ) {
        return ResponseEntity.ok(priorizacionCategoriasService.guardarPriorizacion(grupoId, request));
    }

    @PutMapping
    public ResponseEntity<CategoriaPriorizacionResponseDTO> actualizarPriorizacion(
            @PathVariable Long grupoId,
            @RequestBody CategoriaPriorizacionRequestDTO request
    ) {
        return ResponseEntity.ok(priorizacionCategoriasService.guardarPriorizacion(grupoId, request));
    }
}
