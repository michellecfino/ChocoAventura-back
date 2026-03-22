package com.chocoaventura.services;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Pago;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.PagoRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PagoService {

    @Autowired
    private PagoRepository pagoRepository;

    @Autowired
    private GrupoViajeRepository grupoViajeRepository;

    // =========================
    // CRUD básico
    // =========================

    public Pago create(Pago pago) {
        return pagoRepository.save(pago);
    }

    public List<Pago> getAll() {
        return pagoRepository.findAll();
    }

    public Pago getById(Long id) {
        return pagoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));
    }

    public Pago update(Long id, Pago datos) {
        Pago pago = pagoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));

        pago.setNombre(datos.getNombre());
        pago.setMontoTotal(datos.getMontoTotal());
        pago.setFecha(datos.getFecha());
        pago.setGrupoViaje(datos.getGrupoViaje());

        return pagoRepository.save(pago);
    }

    public void delete(Long id) {
        Pago pago = pagoRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Pago no encontrado con id: " + id));
        pagoRepository.delete(pago);
    }

    // =========================
    // Lógica
    // =========================

    public Pago registrarPago(String nombre, Double montoTotal, LocalDate fecha, Long grupoViajeId) {
        GrupoViaje grupoViaje = grupoViajeRepository.findById(grupoViajeId).orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + grupoViajeId));
        Pago pago = new Pago(nombre, montoTotal, fecha, grupoViaje);
        return pagoRepository.save(pago);
    }
}