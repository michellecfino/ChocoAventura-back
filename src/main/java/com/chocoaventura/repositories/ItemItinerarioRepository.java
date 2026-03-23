package com.chocoaventura.repositories;

import com.chocoaventura.entities.ItemItinerario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;

public interface ItemItinerarioRepository extends JpaRepository<ItemItinerario, Long> {
    List<ItemItinerario> findByItinerarioId(Long itinerarioId);
    List<ItemItinerario> findByItinerarioIdAndEstado(Long itinerarioId, String estado);
    List<ItemItinerario> findByItinerarioIdAndInicioProgramadoBetween(Long itinerarioId, LocalDateTime inicio, LocalDateTime fin);
}