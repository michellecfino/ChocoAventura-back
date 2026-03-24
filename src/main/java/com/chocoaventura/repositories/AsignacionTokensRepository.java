package com.chocoaventura.repositories;

import com.chocoaventura.entities.AsignacionTokens;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionTokensRepository extends JpaRepository<AsignacionTokens, Long> {
    List<AsignacionTokens> findByPerfilId(Long perfilId);
    List<AsignacionTokens> findByActividadId(Long actividadId);
    List<AsignacionTokens> findByRondaSubastaId(Long rondaSubastaId);
    List<AsignacionTokens> findByRondaSubastaIdAndPerfilId(Long rondaSubastaId, Long perfilId);
    List<AsignacionTokens> findByRondaSubastaIdAndActividadId(Long rondaSubastaId, Long actividadId);
    Optional<AsignacionTokens> findByPerfilIdAndActividadIdAndRondaSubastaId(Long perfilId, Long actividadId, Long rondaSubastaId);
}