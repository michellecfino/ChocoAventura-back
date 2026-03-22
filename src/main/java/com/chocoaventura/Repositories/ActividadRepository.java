package com.chocoaventura.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import com.chocoaventura.entities.Actividad;

import jakarta.transaction.Transactional;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
    boolean existsByNombre(String nombre);

    @Modifying
    @Transactional
    @Query(value = "ALTER SEQUENCE actividades_id_seq RESTART WITH 1", nativeQuery = true)
    void resetSequence();
    boolean existsByNombreIgnoreCase(String nombre);
    
}
