package com.chocoaventura.services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Deuda;
import com.chocoaventura.entities.Pago;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.repositories.DeudaRepository;
import com.chocoaventura.repositories.PagoRepository;
import com.chocoaventura.repositories.PerfilRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class DeudaService {

    @Autowired
    private DeudaRepository deudaRepository;

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private PagoRepository pagoRepository;

    // =========================
    // CRUD básico
    // =========================

    public Deuda create(Deuda deuda) {
        return deudaRepository.save(deuda);
    }

    public List<Deuda> getAll() {
        return deudaRepository.findAll();
    }

    public Deuda getById(Long id) {
        return deudaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Deuda no encontrada con id: " + id));
    }

    public Deuda update(Long id, Deuda datos) {
        Deuda deuda = deudaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Deuda no encontrada con id: " + id));

        deuda.setMonto(datos.getMonto());
        deuda.setSaldada(datos.getSaldada());
        deuda.setDeudor(datos.getDeudor());
        deuda.setAcreedor(datos.getAcreedor());
        deuda.setPagoOrigen(datos.getPagoOrigen());

        return deudaRepository.save(deuda);
    }

    public void delete(Long id) {
        Deuda deuda = deudaRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Deuda no encontrada con id: " + id));
        deudaRepository.delete(deuda);
    }

    // =========================
    // Lógica
    // =========================

    public Deuda crearDeuda(Double monto, Long deudorId, Long acreedorId, Long pagoOrigenId) {
        Perfil deudor = perfilRepository.findById(deudorId).orElseThrow(() -> new EntityNotFoundException("Perfil deudor no encontrado con id: " + deudorId));
        Perfil acreedor = perfilRepository.findById(acreedorId).orElseThrow(() -> new EntityNotFoundException("Perfil acreedor no encontrado con id: " + acreedorId));
        Pago pagoOrigen = pagoRepository.findById(pagoOrigenId).orElseThrow(() -> new EntityNotFoundException("Pago origen no encontrado con id: " + pagoOrigenId));

        Deuda deuda = new Deuda(monto, deudor, acreedor, pagoOrigen);
        return deudaRepository.save(deuda);
    }

    public void marcarComoSaldada(Long deudaId) {
        Deuda deuda = deudaRepository.findById(deudaId).orElseThrow(() -> new EntityNotFoundException("Deuda no encontrada con id: " + deudaId));
        deuda.setSaldada(true);
        deudaRepository.save(deuda);
    }
}