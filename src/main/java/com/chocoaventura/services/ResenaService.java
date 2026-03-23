package com.chocoaventura.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.Resena;
import com.chocoaventura.repositories.ActividadRepository;
import com.chocoaventura.repositories.PerfilRepository;
import com.chocoaventura.repositories.ResenaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ResenaService {

    @Autowired
    private ResenaRepository resenaRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    // =========================
    // CRUD básico
    // =========================

    public Resena create(Resena resena) {
        return resenaRepository.save(resena);
    }

    public List<Resena> getAll() {
        return resenaRepository.findAll();
    }

    public Resena getById(Long id) {
        return resenaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada con id: " + id));
    }

    public Resena update(Long id, Resena datos) {
        Resena resena = resenaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada con id: " + id));

        resena.setCalificacion(datos.getCalificacion());
        resena.setComentario(datos.getComentario());
        resena.setFecha(datos.getFecha());
        resena.setPerfil(datos.getPerfil());
        resena.setActividad(datos.getActividad());

        return resenaRepository.save(resena);
    }

    public void delete(Long id) {
        Resena resena = resenaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Reseña no encontrada con id: " + id));
        resenaRepository.delete(resena);
    }

    // =========================
    // Lógica
    // =========================

    public Resena crearResena(Integer calificacion, LocalDate fecha, Long perfilId, Long actividadId) {
        Perfil perfil = perfilRepository.findById(perfilId).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + perfilId));
        Actividad actividad = actividadRepository.findById(actividadId).orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con id: " + actividadId));

        Resena resena = new Resena(calificacion, fecha, perfil, actividad);
        return resenaRepository.save(resena);
    }
}