package com.chocoaventura.controllers;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.dto.auth.LoginRequestDTO;
import com.chocoaventura.dto.auth.LoginResponseDTO;
import com.chocoaventura.entities.Usuario;
import com.chocoaventura.services.UsuarioService;

@RestController
@RequestMapping("/usuarios")
@CrossOrigin(origins = "*")
public class UsuarioController {

    @Autowired
    private UsuarioService usuarioService;

    @PostMapping
    public ResponseEntity<Usuario> create(@RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.create(usuario));
    }

    @GetMapping
    public List<Usuario> getAll() {
        return usuarioService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<Usuario> getById(@PathVariable Long id) {
        return ResponseEntity.ok(usuarioService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Usuario> update(@PathVariable Long id, @RequestBody Usuario usuario) {
        return ResponseEntity.ok(usuarioService.update(id, usuario));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        usuarioService.delete(id);
        return ResponseEntity.ok("Usuario eliminado correctamente");
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequestDTO request) {
        try {
            Usuario usuario = usuarioService.login(request.correo(), request.contrasena());
            return ResponseEntity.ok(new LoginResponseDTO(usuario.getId(), usuario.getNombre(), usuario.getCorreo()));
        } catch (jakarta.persistence.EntityNotFoundException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/registro")
    public ResponseEntity<?> registro(@RequestBody Usuario usuario) {
        try {
            if (usuarioService.existePorCorreo(usuario.getCorreo())) {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body(Map.of("error", "Ya existe una cuenta con este correo."));
            }
            Usuario creado = usuarioService.create(usuario);
            return ResponseEntity.ok(new LoginResponseDTO(creado.getId(), creado.getNombre(), creado.getCorreo()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "No se pudo crear la cuenta. Intenta de nuevo."));
        }
    }
}