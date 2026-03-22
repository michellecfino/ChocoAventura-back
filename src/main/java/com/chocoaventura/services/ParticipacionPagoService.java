package com.chocoaventura.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Pago;
import com.chocoaventura.entities.ParticipacionPago;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.repositories.PagoRepository;
import com.chocoaventura.repositories.ParticipacionPagoRepository;
import com.chocoaventura.repositories.PerfilRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ParticipacionPagoService {

    @Autowired
    private ParticipacionPagoRepository participacionPagoRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PagoRepository pagoRepository;

    // =========================
    // CRUD básico
    // =========================

    public ParticipacionPago create(ParticipacionPago participacionPago) {
        return participacionPagoRepository.save(participacionPago);
    }

    public List<ParticipacionPago> getAll() {
        return participacionPagoRepository.findAll();
    }

    public ParticipacionPago getById(Long id) {
        return participacionPagoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Participación de pago no encontrada con id: " + id));
    }

    public ParticipacionPago update(Long id, ParticipacionPago datos) {
        ParticipacionPago participacion = participacionPagoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Participación de pago no encontrada con id: " + id));

        participacion.setMontoPagado(datos.getMontoPagado());
        participacion.setPorcentajeResponsabilidad(datos.getPorcentajeResponsabilidad());
        participacion.setPerfil(datos.getPerfil());
        participacion.setPago(datos.getPago());

        return participacionPagoRepository.save(participacion);
    }

    public void delete(Long id) {
        ParticipacionPago participacion = participacionPagoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Participación de pago no encontrada con id: " + id));
        participacionPagoRepository.delete(participacion);
    }

    // =========================
    // Lógica
    // =========================

    public ParticipacionPago registrarParticipacion(Double montoPagado, Double porcentajeResponsabilidad, Long perfilId, Long pagoId) {
        Perfil perfil = perfilRepository.findById(perfilId).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + perfilId));
        Pago pago = pagoRepository.findById(pagoId).orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + pagoId));

        ParticipacionPago participacion = new ParticipacionPago(montoPagado, porcentajeResponsabilidad, perfil, pago);
        return participacionPagoRepository.save(participacion);
    }
}