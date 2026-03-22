package com.chocoaventura.repositories;

import com.chocoaventura.entities.Resena;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ResenaRepository extends JpaRepository<Resena, Long> {
    List<Resena> findByActividadId(Long actividadId);
    List<Resena> findByPerfilId(Long perfilId);
}