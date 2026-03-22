package com.chocoaventura.repositories;

import com.chocoaventura.entities.Actividad;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {

    boolean existsByNombre(String nombre);

    boolean existsByNombreIgnoreCase(String nombre);

    List<Actividad> findByNombreContainingIgnoreCase(String nombre);

    List<Actividad> findByVigenciaInicioGreaterThanEqual(LocalDate fecha);

    @Modifying
    @Transactional
    @Query(value = "ALTER SEQUENCE actividades_id_seq RESTART WITH 1", nativeQuery = true)
    void resetSequence();
}