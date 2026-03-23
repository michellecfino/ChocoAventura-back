package com.chocoaventura.repositories;

import com.chocoaventura.entities.Ciudad;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CiudadRepository extends JpaRepository<Ciudad, Long> {
    Optional<Ciudad> findByNombreIgnoreCase(String nombre);
    List<Ciudad> findByNombreContainingIgnoreCase(String nombre);
    List<Ciudad> findByPaisIgnoreCase(String pais);
}