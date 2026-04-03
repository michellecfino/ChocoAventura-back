package com.chocoaventura.services.scrapers;

import com.chocoaventura.entities.Actividad;
import java.util.List;

public interface Scraper {
    List<Actividad> scrapear();
}