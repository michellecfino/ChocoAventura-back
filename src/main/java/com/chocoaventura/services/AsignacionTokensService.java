package com.chocoaventura.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.AsignacionTokens;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.RondaSubasta;
import com.chocoaventura.repositories.ActividadRepository;
import com.chocoaventura.repositories.AsignacionTokensRepository;
import com.chocoaventura.repositories.PerfilRepository;
import com.chocoaventura.repositories.RondaSubastaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class AsignacionTokensService {

    @Autowired
    private AsignacionTokensRepository asignacionTokensRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private RondaSubastaRepository rondaSubastaRepository;

    // =========================
    // CRUD básico
    // =========================

    public AsignacionTokens create(AsignacionTokens asignacionTokens) {
        return asignacionTokensRepository.save(asignacionTokens);
    }

    public List<AsignacionTokens> getAll() {
        return asignacionTokensRepository.findAll();
    }

    public AsignacionTokens getById(Long id) {
        return asignacionTokensRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Asignación de tokens no encontrada con id: " + id));
    }

    public AsignacionTokens update(Long id, AsignacionTokens datos) {
        AsignacionTokens asignacion = asignacionTokensRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Asignación de tokens no encontrada con id: " + id));

        asignacion.setTokensAsignados(datos.getTokensAsignados());
        asignacion.setPerfil(datos.getPerfil());
        asignacion.setActividad(datos.getActividad());
        asignacion.setRondaSubasta(datos.getRondaSubasta());

        return asignacionTokensRepository.save(asignacion);
    }

    public void delete(Long id) {
        AsignacionTokens asignacion = asignacionTokensRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Asignación de tokens no encontrada con id: " + id));
        asignacionTokensRepository.delete(asignacion);
    }

    // =========================
    // Lógica
    // =========================

    public AsignacionTokens asignarTokens(Long perfilId, Long actividadId, Long rondaSubastaId, Integer tokensAsignados) {
        Perfil perfil = perfilRepository.findById(perfilId).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + perfilId));
        Actividad actividad = actividadRepository.findById(actividadId).orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con id: " + actividadId));
        RondaSubasta rondaSubasta = rondaSubastaRepository.findById(rondaSubastaId).orElseThrow(() -> new EntityNotFoundException("Ronda no encontrada con id: " + rondaSubastaId));

        AsignacionTokens asignacion = new AsignacionTokens(tokensAsignados, perfil, actividad, rondaSubasta);
        return asignacionTokensRepository.save(asignacion);
    }
}