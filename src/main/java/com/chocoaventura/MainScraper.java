package com.chocoaventura;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.services.scrapers.Scraper;
import com.chocoaventura.services.scrapers.ScraperBogota;
import com.chocoaventura.services.scrapers.ScraperEventario;
import com.chocoaventura.services.scrapers.manager.ScraperManager;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;
import java.util.List;

public class MainScraper {

    public static void main(String[] args) {

        System.out.println("niciando scraper...");

        Scraper scraper = new ScraperBogota();

        //scrapper de bogota, medellín y cartagena 
        Scraper scraperEventario = new ScraperEventario();

        List<Actividad> lista = scraper.scrapear();
        lista.addAll(scraperEventario.scrapear());
        //Eso lo dejo para hacer un seguimiento del scraping y ver si algo sale mal jiji
        System.out.println("Eventos obtenidos: " + lista.size());

        generarReporte(lista);
    }

    //Esto me hacía un reporte bien bonito jiji pero pues sólo para probar, lo podemos omitir c:
    private static void generarReporte(List<Actividad> lista) {
        File archivo = new File("reporte_choco_FINAL.txt");

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(archivo))) {

            writer.write("=== REPORTE CHOCOAVENTURAS ===\n");
            writer.write("Fecha: " + LocalDateTime.now() + "\n");
            writer.write("Eventos: " + lista.size() + "\n\n");

            for (Actividad a : lista) {
                writer.write( a.getNombre() + "\n");
                writer.write(  a.getDescripcion() + "\n");
                writer.write( a.getUbicacion().getNombre() + "\n");
                writer.write( a.getFuenteUrl() + "\n");
                writer.write( a.getImagenUrl() + "\n");

                writer.write(
                        a.getUbicacion().getDireccion() != null
                                ? "📌 " + a.getUbicacion().getDireccion() + "\n"
                                : "📌 Dirección no disponible\n"
                );

                if (a.getVigenciaInicio() != null && a.getVigenciaFin() != null) {
                    if (a.getVigenciaInicio().equals(a.getVigenciaFin())) {
                        writer.write( a.getVigenciaInicio() + "\n");
                    } else {
                        writer.write( a.getVigenciaInicio() + " - " + a.getVigenciaFin() + "\n");
                    }
                } else {
                    writer.write("Fecha no disponible\n");
                }

                writer.write("====================================\n\n");
            }

            System.out.println(archivo.getAbsolutePath());

        } catch (Exception e) {
            System.err.println("Error escribiendo archivo");
        }
    }
}