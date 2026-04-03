package com.chocoaventura.services.scrapers.manager;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.services.scrapers.Scraper;
import com.chocoaventura.services.scrapers.ScraperBogota;

import java.util.ArrayList;
import java.util.List;

public class ScraperManager {

    private final List<Scraper> scrapers = new ArrayList<>();

    public ScraperManager() {
        scrapers.add(new ScraperBogota());
    }

    public List<Actividad> ejecutarTodos() {
        List<Actividad> todas = new ArrayList<>();

        for (Scraper scraper : scrapers) {
            try {
                System.out.println("🚀 Ejecutando: " + scraper.getClass().getSimpleName());
                todas.addAll(scraper.scrapear());
            } catch (Exception e) {
                System.out.println("⚠️ Error en scraper: " + scraper.getClass().getSimpleName());
                e.printStackTrace();
            }
        }

        return todas;
    }
}
