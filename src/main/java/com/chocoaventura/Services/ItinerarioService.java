package com.chocoaventura.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Itinerario;
import com.chocoaventura.Repositories.GrupoViajeRepository;
import com.chocoaventura.Repositories.ItinerarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ItinerarioService {

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @Autowired
    private GrupoViajeRepository grupoViajeRepository;

    // =========================
    // CRUD básico
    // =========================

    public Itinerario create(Itinerario itinerario) {
        return itinerarioRepository.save(itinerario);
    }

    public List<Itinerario> getAll() {
        return itinerarioRepository.findAll();
    }

    public Itinerario getById(Long id) {
        return itinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Itinerario no encontrado con id: " + id));
    }

    public Itinerario update(Long id, Itinerario datos) {
        Itinerario itinerario = itinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Itinerario no encontrado con id: " + id));

        itinerario.setNombre(datos.getNombre());
        itinerario.setPresupuestoPromedioPersona(datos.getPresupuestoPromedioPersona());
        itinerario.setGrupoViaje(datos.getGrupoViaje());

        return itinerarioRepository.save(itinerario);
    }

    public void delete(Long id) {
        Itinerario itinerario = itinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Itinerario no encontrado con id: " + id));
        itinerarioRepository.delete(itinerario);
    }

    // =========================
    // Lógica
    // =========================

    public Itinerario crearItinerario(String nombre, Double presupuestoPromedioPersona, Long grupoViajeId) {
        GrupoViaje grupoViaje = grupoViajeRepository.findById(grupoViajeId).orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + grupoViajeId));
        Itinerario itinerario = new Itinerario(nombre, presupuestoPromedioPersona, grupoViaje);
        return itinerarioRepository.save(itinerario);
    }
}