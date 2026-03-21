package com.chocoaventura.Controllers;

import java.util.List;

import org.springframework.web.bind.annotation.*;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.Services.ActividadService;

@RestController
@RequestMapping("/actividades")
public class ActividadController {

    private final ActividadService scraperService;

    public ActividadController(ActividadService scraperService) {
        this.scraperService = scraperService;
    }

    @GetMapping("/scrapear")
    public String scrapear() {
        scraperService.scrapearIdartes();
        return "OY EL CODIGO NUEVO 2026";
    }

    @GetMapping
    public List<Actividad> getActividades() {
        return scraperService.getAll();
    }
}