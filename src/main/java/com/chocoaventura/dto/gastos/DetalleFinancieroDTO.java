package com.chocoaventura.dto.gastos;

import java.util.List;
import java.util.Map;

public record DetalleFinancieroDTO(
        Double presupuestoTotal,
        Double gastado,
        Double restante,
        List<PersonaMontoDTO> personasTuDebes,
        List<PersonaMontoDTO> personasTeDeben,
        Map<String, Double> resumenPorCategoria,
        List<GastoRecienteDTO> gastosRecientes,
        String recomendacionChoco,
        boolean todoSaldado
) {
}
