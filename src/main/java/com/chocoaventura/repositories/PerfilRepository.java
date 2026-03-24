package com.chocoaventura.repositories;

import com.chocoaventura.entities.Perfil;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PerfilRepository extends JpaRepository<Perfil, Long> {
    List<Perfil> findByUsuarioId(Long usuarioId);
    List<Perfil> findByGrupoViajeId(Long grupoViajeId);
    Optional<Perfil> findByUsuarioIdAndGrupoViajeId(Long usuarioId, Long grupoViajeId);
    boolean existsByUsuarioIdAndGrupoViajeId(Long usuarioId, Long grupoViajeId);
    List<Perfil> findByGrupoViajeIdAndFaseIndividualListaTrue(Long grupoViajeId);
    List<Perfil> findByGrupoViajeIdAndParticipaEnCoordinacionTrue(Long grupoViajeId);
    List<Perfil> findByGrupoViajeIdAndParticipaEnCoordinacionTrueAndFaseIndividualListaTrue(Long grupoViajeId);
}