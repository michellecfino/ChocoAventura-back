package com.chocoaventura.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.DiaSemana;
import com.chocoaventura.Repositories.DiaSemanaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DiaSemanaService {

    @Autowired
    private DiaSemanaRepository diaSemanaRepository;

    // =========================
    // CRUD básico
    // =========================

    public DiaSemana create(DiaSemana diaSemana) {
        return diaSemanaRepository.save(diaSemana);
    }

    public List<DiaSemana> getAll() {
        return diaSemanaRepository.findAll();
    }

    public DiaSemana getById(Long id) {
        return diaSemanaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("DiaSemana no encontrado con id: " + id));
    }

    public DiaSemana update(Long id, DiaSemana datos) {
        DiaSemana diaSemana = diaSemanaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("DiaSemana no encontrado con id: " + id));

        diaSemana.setLunes(datos.getLunes());
        diaSemana.setMartes(datos.getMartes());
        diaSemana.setMiercoles(datos.getMiercoles());
        diaSemana.setJueves(datos.getJueves());
        diaSemana.setViernes(datos.getViernes());
        diaSemana.setSabado(datos.getSabado());
        diaSemana.setDomingo(datos.getDomingo());

        return diaSemanaRepository.save(diaSemana);
    }

    public void delete(Long id) {
        DiaSemana diaSemana = diaSemanaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("DiaSemana no encontrado con id: " + id));
        diaSemanaRepository.delete(diaSemana);
    }
}