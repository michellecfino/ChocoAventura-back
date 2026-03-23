package com.chocoaventura.services;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.RondaSubasta;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.RondaSubastaRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class RondaSubastaService {

    @Autowired
    private RondaSubastaRepository rondaSubastaRepository;

    @Autowired
    private GrupoViajeRepository grupoViajeRepository;

    // =========================
    // CRUD básico
    // =========================

    public RondaSubasta create(RondaSubasta rondaSubasta) {
        return rondaSubastaRepository.save(rondaSubasta);
    }

    public List<RondaSubasta> getAll() {
        return rondaSubastaRepository.findAll();
    }

    public RondaSubasta getById(Long id) {
        return rondaSubastaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ronda de subasta no encontrada con id: " + id));
    }

    public RondaSubasta update(Long id, RondaSubasta datos) {
        RondaSubasta ronda = rondaSubastaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ronda de subasta no encontrada con id: " + id));

        ronda.setFechaFin(datos.getFechaFin());
        ronda.setEstado(datos.getEstado());
        ronda.setTokensPorPerfil(datos.getTokensPorPerfil());
        ronda.setGrupoViaje(datos.getGrupoViaje());
        ronda.setActividadesSubasta(datos.getActividadesSubasta());

        return rondaSubastaRepository.save(ronda);
    }

    public void delete(Long id) {
        RondaSubasta ronda = rondaSubastaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Ronda de subasta no encontrada con id: " + id));
        rondaSubastaRepository.delete(ronda);
    }

    // =========================
    // Lógica
    // =========================

    public RondaSubasta crearRondaParaGrupo(Long grupoId, LocalDateTime fechaFin) {
        GrupoViaje grupo = grupoViajeRepository.findById(grupoId).orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + grupoId));

        long dias = ChronoUnit.DAYS.between(grupo.getFechaHoraLlegada().toLocalDate(), grupo.getFechaHoraSalida().toLocalDate());
        if (dias <= 0) {
            dias = 1;
        }

        int tokensPorPerfil = (int) dias * 5;
        RondaSubasta ronda = new RondaSubasta(fechaFin, tokensPorPerfil, grupo);

        return rondaSubastaRepository.save(ronda);
    }
}