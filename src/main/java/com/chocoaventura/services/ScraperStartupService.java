package com.chocoaventura.services;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.repositories.ActividadRepository;
import com.chocoaventura.services.scrapers.Scraper;
import com.chocoaventura.services.scrapers.ScraperBogota;
import com.chocoaventura.services.scrapers.ScraperEventario;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScraperStartupService {

    @Autowired
    private ActividadRepository actividadRepository;

   @EventListener(ApplicationReadyEvent.class)
    public void runScrapersOnStartup() {
        System.out.println("🚀 Iniciando scraper en startup...");

        Scraper scraper = new ScraperBogota();
        Scraper scraperEventario = new ScraperEventario();

        // 1. Intentamos Bogotá
        try {
            List<Actividad> listaBogota = scraper.scrapear();
            actividadRepository.saveAll(listaBogota);
            System.out.println("✅ Bogotá guardado: " + listaBogota.size());
        } catch (Exception e) {
            System.err.println("❌ Falló Bogotá: " + e.getMessage());
        }

        // 2. Intentamos Eventario (El que está dando problemas)
        try {
            List<Actividad> listaEventario = scraperEventario.scrapear();
            actividadRepository.saveAll(listaEventario);
            System.out.println("✅ Eventario guardado: " + listaEventario.size());
        } catch (Exception e) {
            System.err.println("❌ Falló Eventario en alguna página: " + e.getMessage());
            // Aquí es donde el error 400 te está matando el proceso.
        }
        
        System.out.println("Total en BD: " + actividadRepository.count());
    }
}