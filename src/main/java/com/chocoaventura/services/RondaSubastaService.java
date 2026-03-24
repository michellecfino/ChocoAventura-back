package com.chocoaventura.services;

import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.RondaSubasta;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.RondaSubastaRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class RondaSubastaService {

    @Autowired
    private RondaSubastaRepository rondaSubastaRepository;
    
    @Autowired
    private GrupoViajeRepository grupoViajeRepository;

    public RondaSubasta create(RondaSubasta rondaSubasta) {
        return rondaSubastaRepository.save(rondaSubasta);
    }

    public List<RondaSubasta> getAll() {
        return rondaSubastaRepository.findAll();
    }

    public RondaSubasta getById(Long id) {
        return rondaSubastaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Ronda de subasta no encontrada con id: " + id));
    }

    public RondaSubasta update(Long id, RondaSubasta datos) {
        RondaSubasta ronda = getById(id);
        ronda.setFechaFin(datos.getFechaFin());
        ronda.setEstado(datos.getEstado());
        ronda.setTokensPorPerfil(datos.getTokensPorPerfil());
        ronda.setGrupoViaje(datos.getGrupoViaje());
        ronda.setActividadesSubasta(datos.getActividadesSubasta());
        ronda.setNumeroRonda(datos.getNumeroRonda());
        ronda.setBloqueInicio(datos.getBloqueInicio());
        ronda.setBloqueFin(datos.getBloqueFin());
        return rondaSubastaRepository.save(ronda);
    }

    public void delete(Long id) {
        rondaSubastaRepository.delete(getById(id));
    }

    public RondaSubasta crearRondaParaGrupo(Long grupoId, Integer numeroRonda, LocalDate bloqueInicio, LocalDate bloqueFin, LocalDateTime fechaFin) {
        GrupoViaje grupo = grupoViajeRepository.findById(grupoId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + grupoId));

        long diasBloque = java.time.temporal.ChronoUnit.DAYS.between(bloqueInicio, bloqueFin) + 1;
        if (diasBloque <= 0) {
            diasBloque = 1;
        }

        int tokensPorPerfil = (int) diasBloque * 5;
        RondaSubasta ronda = new RondaSubasta(numeroRonda, bloqueInicio, bloqueFin, fechaFin, tokensPorPerfil, grupo);
        ronda.setEstado("GENERADA");
        return rondaSubastaRepository.save(ronda);
    }
}