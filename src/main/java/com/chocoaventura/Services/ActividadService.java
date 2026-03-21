package com.chocoaventura.Services;

import java.time.LocalDate;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import com.chocoaventura.Entities.Actividad;
import com.chocoaventura.Repositories.ActividadRepository;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    public void scrapearIdartes() {
        try {
            Document doc = Jsoup.connect("https://www.idartes.gov.co/es/agenda").get();
            
            Elements eventos = doc.select(".view-content .views-row"); 

            for (Element e : eventos) {
                String titulo = e.select(".field-name-title-field").text();
                String descCorta = e.select(".field-name-field-resumen").text();

                Actividad actividad = Actividad.builder()
                        .titulo(titulo.isEmpty() ? "Evento sin título" : titulo)
                        .descripcion(descCorta)
                        .costoPorPersona(0.0)
                        .fechaInicio(LocalDate.now())
                        .fuente("Idartes")
                        .build();

                if (!actividad.getTitulo().equals("Evento sin título")) {
                    actividadRepository.save(actividad);
                }
            }
            System.out.println("¡Scraping de Idartes completado con éxito!");

        } catch (Exception e) {
            System.err.println("Error al scrapear Idartes: " + e.getMessage());
        }
    }
}