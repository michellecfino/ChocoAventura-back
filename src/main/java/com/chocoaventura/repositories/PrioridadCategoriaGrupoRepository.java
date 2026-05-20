package com.chocoaventura.repositories;

import com.chocoaventura.entities.PrioridadCategoriaGrupo;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PrioridadCategoriaGrupoRepository extends JpaRepository<PrioridadCategoriaGrupo, Long> {
    List<PrioridadCategoriaGrupo> findByGrupoViajeId(Long grupoViajeId);
    List<PrioridadCategoriaGrupo> findByGrupoViajeIdOrderByPosicionAsc(Long grupoViajeId);
    List<PrioridadCategoriaGrupo> findByGrupoViajeIdAndPerfilIdOrderByPosicionAsc(Long grupoViajeId, Long perfilId);
    List<PrioridadCategoriaGrupo> findByGrupoViajeIdAndPerfilIsNullOrderByPosicionAsc(Long grupoViajeId);
    void deleteByGrupoViajeIdAndPerfilId(Long grupoViajeId, Long perfilId);
    void deleteByGrupoViajeIdAndPerfilIsNull(Long grupoViajeId);
}
