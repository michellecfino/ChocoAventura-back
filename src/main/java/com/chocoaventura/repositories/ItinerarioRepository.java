package com.chocoaventura.repositories;

import com.chocoaventura.entities.Itinerario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ItinerarioRepository extends JpaRepository<Itinerario, Long> {
    List<Itinerario> findByGrupoViajeId(Long grupoViajeId);
}