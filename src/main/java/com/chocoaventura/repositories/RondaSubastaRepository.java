package com.chocoaventura.repositories;

import com.chocoaventura.entities.RondaSubasta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface RondaSubastaRepository extends JpaRepository<RondaSubasta, Long> {
    List<RondaSubasta> findByGrupoViajeId(Long grupoViajeId);
    List<RondaSubasta> findByGrupoViajeIdAndEstado(Long grupoViajeId, String estado);
}
