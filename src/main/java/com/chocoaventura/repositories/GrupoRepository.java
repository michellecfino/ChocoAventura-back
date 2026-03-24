package com.chocoaventura.repositories;
import com.chocoaventura.entities.GrupoViaje;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface GrupoRepository extends JpaRepository<GrupoViaje, Long> {

}