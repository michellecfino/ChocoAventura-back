package com.chocoaventura.DTOs;

import java.time.LocalDate;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class DiaItinerarioDTO {
    private LocalDate fecha;
    private List<ItemItinerarioResponseDTO> items;
}