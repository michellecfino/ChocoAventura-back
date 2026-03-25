package com.chocoaventura.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Usuario;
import com.chocoaventura.repositories.UsuarioRepository;

@Service
public class AutenticacionService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    public Usuario registrar(Usuario nuevoUsuario, String confirmacionPassword) {
        
        if (!nuevoUsuario.getContrasena().equals(confirmacionPassword)) {
            throw new IllegalArgumentException("Las contraseñas no coinciden");
        }

        if (usuarioRepository.existsByCorreo(nuevoUsuario.getCorreo())) {
            throw new IllegalArgumentException("El correo ya está registrado");
        }

        return usuarioRepository.save(nuevoUsuario);
    }

    public Usuario iniciarSesion(String correo, String password) {
    
        Usuario usuario = usuarioRepository.findByCorreo(correo)
                .orElseThrow(() -> new IllegalArgumentException("Usuario no encontrado"));

     
        if (!usuario.getContrasena().equals(password)) {
            throw new IllegalArgumentException("Contraseña incorrecta");
        }

        return usuario;
    }
    
}
