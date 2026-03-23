package com.chocoaventura.repositories;

import com.chocoaventura.entities.Imagen;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ImagenRepository extends JpaRepository<Imagen, Long> {
    List<Imagen> findByActividadId(Long actividadId);
}