package com.chocoaventura.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Horario;
import com.chocoaventura.Repositories.HorarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class HorarioService {

    @Autowired
    private HorarioRepository horarioRepository;

    // =========================
    // CRUD básico
    // =========================

    public Horario create(Horario horario) {
        return horarioRepository.save(horario);
    }

    public List<Horario> getAll() {
        return horarioRepository.findAll();
    }

    public Horario getById(Long id) {
        return horarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Horario no encontrado con id: " + id));
    }

    public Horario update(Long id, Horario datos) {
        Horario horario = horarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Horario no encontrado con id: " + id));

        horario.setHoraInicio(datos.getHoraInicio());
        horario.setHoraFin(datos.getHoraFin());
        horario.setActividad(datos.getActividad());
        horario.setDiasSemana(datos.getDiasSemana());

        return horarioRepository.save(horario);
    }

    public void delete(Long id) {
        Horario horario = horarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Horario no encontrado con id: " + id));
        horarioRepository.delete(horario);
    }
}