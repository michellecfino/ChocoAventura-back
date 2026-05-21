package com.chocoaventura.DTOs;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CategoriaPriorizacionRequestDTO {
    private Long usuarioId;
    private Long perfilId;
    private List<Long> categoriaIds;
    private List<String> categorias;
    private List<CategoriaRankingItemDTO> ranking;
}
