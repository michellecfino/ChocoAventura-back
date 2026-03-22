package com.chocoaventura.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Ubicacion;
import com.chocoaventura.Repositories.UbicacionRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class UbicacionService {

    @Autowired
    private UbicacionRepository ubicacionRepository;

    // =========================
    // CRUD básico
    // =========================

    public Ubicacion create(Ubicacion ubicacion) {
        return ubicacionRepository.save(ubicacion);
    }

    public List<Ubicacion> getAll() {
        return ubicacionRepository.findAll();
    }

    public Ubicacion getById(Long id) {
        return ubicacionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ubicación no encontrada con id: " + id));
    }

    public Ubicacion update(Long id, Ubicacion datos) {
        Ubicacion ubicacion = ubicacionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ubicación no encontrada con id: " + id));

        ubicacion.setNombre(datos.getNombre());
        ubicacion.setDireccion(datos.getDireccion());
        ubicacion.setLatitud(datos.getLatitud());
        ubicacion.setLongitud(datos.getLongitud());

        return ubicacionRepository.save(ubicacion);
    }

    public void delete(Long id) {
        Ubicacion ubicacion = ubicacionRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ubicación no encontrada con id: " + id));
        ubicacionRepository.delete(ubicacion);
    }
}