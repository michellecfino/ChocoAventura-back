package com.chocoaventura.repositories;

import com.chocoaventura.entities.Ubicacion;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

public interface UbicacionRepository extends JpaRepository<Ubicacion, Long> {
        List<Ubicacion> findByNombreIgnoreCase(String nombre);
        List<Ubicacion> findByDireccionIgnoreCase(String direccion);
        List<Ubicacion> findByLatitudAndLongitud(Double latitud, Double longitud);
        List<Ubicacion> findByDireccionAndLatitudAndLongitudList(String direccion, Double latitud, Double longitud);

}