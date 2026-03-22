package com.chocoaventura.repositories;

import com.chocoaventura.entities.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface PagoRepository extends JpaRepository<Pago, Long> {
    List<Pago> findByGrupoViajeId(Long grupoViajeId);
    List<Pago> findByFechaBetween(LocalDate fechaInicio, LocalDate fechaFin);
}