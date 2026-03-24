package com.chocoaventura.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Ciudad;
import com.chocoaventura.repositories.CiudadRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class CiudadService {

    @Autowired
    private CiudadRepository ciudadRepository;

    // =========================
    // CRUD básico
    // =========================

    public Ciudad create(Ciudad ciudad) {
        return ciudadRepository.save(ciudad);
    }

    public List<Ciudad> getAll() {
        return ciudadRepository.findAll();
    }

    public Ciudad getById(Long id) {
        return ciudadRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ciudad no encontrada con id: " + id));
    }

    public Ciudad update(Long id, Ciudad datos) {
        Ciudad ciudad = ciudadRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ciudad no encontrada con id: " + id));

        ciudad.setNombre(datos.getNombre());
        ciudad.setPais(datos.getPais());

        return ciudadRepository.save(ciudad);
    }

    public void delete(Long id) {
        Ciudad ciudad = ciudadRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ciudad no encontrada con id: " + id));
        ciudadRepository.delete(ciudad);
    }
}