package com.chocoaventura.repositories;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.chocoaventura.entities.Ciudad;

public interface CiudadRepository extends JpaRepository<Ciudad, Long> {
    List<Ciudad> findByNombreIgnoreCase(String nombre);
    List<Ciudad> findByNombreContainingIgnoreCase(String nombre);
    List<Ciudad> findByPaisIgnoreCase(String pais);
    List<Ciudad> findByNombreIgnoreCaseAndPaisIgnoreCase(String nombre, String pais);

}