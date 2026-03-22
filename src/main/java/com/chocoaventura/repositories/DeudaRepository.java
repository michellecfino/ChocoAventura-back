package com.chocoaventura.repositories;

import com.chocoaventura.entities.Deuda;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeudaRepository extends JpaRepository<Deuda, Long> {
    List<Deuda> findByDeudorId(Long deudorId);
    List<Deuda> findByAcreedorId(Long acreedorId);
    List<Deuda> findByPagoOrigenId(Long pagoOrigenId);
    List<Deuda> findByDeudorIdAndSaldadaFalse(Long deudorId);
    List<Deuda> findByAcreedorIdAndSaldadaFalse(Long acreedorId);
    List<Deuda> findByDeudorIdAndAcreedorIdAndSaldadaFalse(Long deudorId, Long acreedorId);
}