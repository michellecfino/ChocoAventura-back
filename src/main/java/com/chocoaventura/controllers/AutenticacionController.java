package com.chocoaventura.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.chocoaventura.entities.Usuario;
import com.chocoaventura.services.AutenticacionService;

@RestController
@RequestMapping("/auth")
public class AutenticacionController {

    @Autowired
    private AutenticacionService autenticacionService;

    @PostMapping("/registrar")
    public ResponseEntity<Usuario> registrar(@RequestBody Usuario nuevoUsuario, @RequestParam String confirmacionPassword) {
        return ResponseEntity.ok(autenticacionService.registrar(nuevoUsuario, confirmacionPassword));
    }

    @PostMapping("/login")
    public ResponseEntity<Usuario> iniciarSesion(@RequestParam String correo, @RequestParam String password) {
        return ResponseEntity.ok(autenticacionService.iniciarSesion(correo, password));
    }
    
}
