package com.chocoaventura.repositories;

import com.chocoaventura.entities.Itinerario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ItinerarioRepository extends JpaRepository<Itinerario, Long> {
    List<Itinerario> findByGrupoViajeId(Long grupoViajeId);

    @Query("select distinct i from Itinerario i join i.grupoViaje g join g.perfiles p where p.usuario.id = :usuarioId")
    List<Itinerario> findByUsuarioParticipante(@Param("usuarioId") Long usuarioId);

    @Query("select case when count(i) > 0 then true else false end from Itinerario i join i.grupoViaje g join g.perfiles p where i.id = :itinerarioId and p.usuario.id = :usuarioId")
    boolean existsByIdAndUsuarioParticipante(@Param("itinerarioId") Long itinerarioId, @Param("usuarioId") Long usuarioId);
}