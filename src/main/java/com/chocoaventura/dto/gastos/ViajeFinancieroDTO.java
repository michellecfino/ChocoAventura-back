package com.chocoaventura.dto.gastos;

public record ViajeFinancieroDTO(
        Long idViaje,
        Long idGrupo,
        String nombreViaje,
        String estado,
        Double tuDebes,
        Double teDeben,
        Double hasGastado,
        Double presupuesto,
        String destinoKey,
        Long perfilId
) {
}
