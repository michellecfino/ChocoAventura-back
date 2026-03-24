package com.chocoaventura.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Imagen;
import com.chocoaventura.repositories.ImagenRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ImagenService {

    @Autowired
    private ImagenRepository imagenRepository;

    // =========================
    // CRUD básico
    // =========================

    public Imagen create(Imagen imagen) {
        return imagenRepository.save(imagen);
    }

    public List<Imagen> getAll() {
        return imagenRepository.findAll();
    }

    public Imagen getById(Long id) {
        return imagenRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con id: " + id));
    }

    public Imagen update(Long id, Imagen datos) {
        Imagen imagen = imagenRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con id: " + id));

        imagen.setUrl(datos.getUrl());
        imagen.setActividad(datos.getActividad());

        return imagenRepository.save(imagen);
    }

    public void delete(Long id) {
        Imagen imagen = imagenRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Imagen no encontrada con id: " + id));
        imagenRepository.delete(imagen);
    }
}