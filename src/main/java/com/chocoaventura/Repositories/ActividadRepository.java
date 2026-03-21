package com.chocoaventura.Repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import com.chocoaventura.Entities.Actividad;

public interface ActividadRepository extends JpaRepository<Actividad, Long> {
} 
