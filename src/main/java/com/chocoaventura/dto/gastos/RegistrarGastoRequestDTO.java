package com.chocoaventura.dto.gastos;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.util.List;

public record RegistrarGastoRequestDTO(
        @NotNull Long grupoId,
        @NotNull Long perfilId,
        @NotBlank String descripcion,
        @NotNull @Positive Double monto,
        @NotBlank String tipo,
        String categoria,
        Long pagadoPorPerfilId,
        List<Long> participantesIds,
        String division,
        String nota,
        String detalleDivision
) {
}
