package com.chocoaventura.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.chocoaventura.Entities.Actividad;

import jakarta.transaction.Transactional;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    boolean existsByTitulo(String titulo);

    @Modifying
    @Transactional
    @Query(value = "ALTER SEQUENCE actividades_id_seq RESTART WITH 1", nativeQuery = true)
    void resetSequence();
    boolean existsByTituloIgnoreCase(String titulo);
    
}
