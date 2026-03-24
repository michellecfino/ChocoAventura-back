package com.chocoaventura.repositories;

import com.chocoaventura.entities.BloqueExploracionGrupal;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BloqueExploracionGrupalRepository extends JpaRepository<BloqueExploracionGrupal, Long> {
    List<BloqueExploracionGrupal> findByRondaSubastaIdOrderByOrdenVisualAsc(Long rondaSubastaId);
}