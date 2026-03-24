package com.chocoaventura.repositories;

import com.chocoaventura.entities.Ciudad;
import com.chocoaventura.entities.Ubicacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
        List<Ubicacion> findByNombreIgnoreCase(String nombre);

}