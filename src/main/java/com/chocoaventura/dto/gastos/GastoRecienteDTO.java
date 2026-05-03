package com.chocoaventura.dto.gastos;

import java.time.LocalDate;

public record GastoRecienteDTO(Long id, String descripcion, Double monto, LocalDate fecha, String categoria) {
}
