package com.chocoaventura.DTOs;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CategoriaPriorizacionResponseDTO {
    private Long grupoViajeId;
    private Long usuarioId;
    private Long perfilId;
    private List<CategoriaPriorizacionDTO> categoriasDisponibles;
    private List<CategoriaRankingItemDTO> ranking;
    private Map<String, Integer> puntajesPorCategoria;
    private Boolean tienePriorizacion;
    private Integer totalParticipantes;
    private Integer participantesPriorizados;
    private Integer faltanPorPriorizar;
    private Boolean listoParaItinerario;
    private Boolean usuarioActualPriorizo;
    private Boolean usuarioActualDebePriorizar;
    private Boolean usuarioActualPuedePriorizar;
    private String mensaje;
}
