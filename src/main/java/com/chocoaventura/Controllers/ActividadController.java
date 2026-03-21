package com.chocoaventura.Controllers;

import org.springframework.web.bind.annotation.*;
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
        return "Scraping ejecutado";
    }
}