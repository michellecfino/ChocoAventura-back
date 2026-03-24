package com.chocoaventura.repositories;

import com.chocoaventura.entities.GrupoViaje;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GrupoViajeRepository extends JpaRepository<GrupoViaje, Long> {
    List<GrupoViaje> findByDuenoId(Long duenoId);
    List<GrupoViaje> findByCiudadDestinoId(Long ciudadDestinoId);
}