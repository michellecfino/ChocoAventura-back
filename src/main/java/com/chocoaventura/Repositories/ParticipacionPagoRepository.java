package com.chocoaventura.repositories;

import com.chocoaventura.entities.ParticipacionPago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ParticipacionPagoRepository extends JpaRepository<ParticipacionPago, Long> {
    List<ParticipacionPago> findByPerfilId(Long perfilId);
    List<ParticipacionPago> findByPagoId(Long pagoId);
    Optional<ParticipacionPago> findByPerfilIdAndPagoId(Long perfilId, Long pagoId);
}