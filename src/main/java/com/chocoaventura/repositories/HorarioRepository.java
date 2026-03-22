package com.chocoaventura.repositories;

import com.chocoaventura.entities.Horario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalTime;
import java.util.List;

public interface HorarioRepository extends JpaRepository<Horario, Long> {
    List<Horario> findByActividadId(Long actividadId);
    List<Horario> findByDiasSemanaId(Long diasSemanaId);
    List<Horario> findByHoraInicioLessThanEqualAndHoraFinGreaterThanEqual(LocalTime horaInicio, LocalTime horaFin);
}