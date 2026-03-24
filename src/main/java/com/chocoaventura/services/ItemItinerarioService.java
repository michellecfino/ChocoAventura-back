package com.chocoaventura.services;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.ItemItinerario;
import com.chocoaventura.entities.Itinerario;
import com.chocoaventura.repositories.ActividadRepository;
import com.chocoaventura.repositories.ItemItinerarioRepository;
import com.chocoaventura.repositories.ItinerarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ItemItinerarioService {

    @Autowired
    private ItemItinerarioRepository itemItinerarioRepository;

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    // =========================
    // CRUD básico
    // =========================

    public ItemItinerario create(ItemItinerario itemItinerario) {
        return itemItinerarioRepository.save(itemItinerario);
    }

    public List<ItemItinerario> getAll() {
        return itemItinerarioRepository.findAll();
    }

    public ItemItinerario getById(Long id) {
        return itemItinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemItinerario no encontrado con id: " + id));
    }

    public ItemItinerario update(Long id, ItemItinerario datos) {
        ItemItinerario item = itemItinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemItinerario no encontrado con id: " + id));

        item.setInicioProgramado(datos.getInicioProgramado());
        item.setFinProgramado(datos.getFinProgramado());
        item.setEstado(datos.getEstado());
        item.setItinerario(datos.getItinerario());
        item.setActividad(datos.getActividad());

        return itemItinerarioRepository.save(item);
    }

    public void delete(Long id) {
        ItemItinerario item = itemItinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("ItemItinerario no encontrado con id: " + id));
        itemItinerarioRepository.delete(item);
    }

    // =========================
    // Lógica
    // =========================

    public ItemItinerario agregarActividadAItinerario(LocalDateTime inicioProgramado, LocalDateTime finProgramado, Long itinerarioId, Long actividadId) {
        Itinerario itinerario = itinerarioRepository.findById(itinerarioId).orElseThrow(() -> new EntityNotFoundException("Itinerario no encontrado con id: " + itinerarioId));
        Actividad actividad = actividadRepository.findById(actividadId).orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con id: " + actividadId));

        ItemItinerario item = new ItemItinerario(inicioProgramado, finProgramado, itinerario, actividad);
        itinerario.getItems().add(item);
        return itemItinerarioRepository.save(item);
    }
}