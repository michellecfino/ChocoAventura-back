package com.chocoaventura.services.scrapers;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Ubicacion;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.jsoup.Jsoup;

import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class ScraperEventario implements Scraper {

    @Override
    public List<Actividad> scrapear() {

        List<Actividad> lista = new ArrayList<>();

        try {
            ObjectMapper mapper = new ObjectMapper();

            int page = 1;
            boolean hayMas = true;

            while (hayMas) {

                String apiUrl = "https://eventario.co/wp-json/wp/v2/events?per_page=100&page=" + page;

                JsonNode root = mapper.readTree(new URL(apiUrl));

                if (root.size() == 0) {
                    hayMas = false;
                    break;
                }

                for (JsonNode nodo : root) {

                    String titulo = nodo.get("title").get("rendered").asText();

                    String link = nodo.get("link").asText();

                    String descripcionHtml = nodo.get("content").get("rendered").asText();
                    String descripcion = Jsoup.parse(descripcionHtml).text();

                    String imagen = nodo.has("featured_image_url")
                            ? nodo.get("featured_image_url").asText()
                            : "";

                    JsonNode location = nodo.get("eventario_location");

                    String direccion = (location != null && location.has("address"))
                            ? location.get("address").asText()
                            : "Sin dirección";

                    double lat = (location != null && location.has("latitude"))
                            ? location.get("latitude").asDouble()
                            : 0.0;

                    double lon = (location != null && location.has("longitude"))
                            ? location.get("longitude").asDouble()
                            : 0.0;

                    String ciudad = "Colombia";

                    JsonNode barrios = nodo.get("event_barrio_details");

                    if (barrios != null) {
                        for (JsonNode b : barrios) {
                            String nombre = b.get("name").asText().toLowerCase();

                            if (nombre.contains("bogotá") || nombre.contains("bogota")) {
                                ciudad = "Bogotá";
                                break;
                            } else if (nombre.contains("medellin")) {
                                ciudad = "Medellín";
                                break;
                            } else if (nombre.contains("cartagena")) {
                                ciudad = "Cartagena";
                                break;
                            }
                        }
                    }

                    if (ciudad.equals("Colombia") && direccion != null) {
                        if (direccion.contains("Bogotá")) ciudad = "Bogotá";
                        else if (direccion.contains("Medellín")) ciudad = "Medellín";
                        else if (direccion.contains("Cartagena")) ciudad = "Cartagena";
                        else {
                            String[] partes = direccion.split(",");
                            if (partes.length >= 2) {
                                ciudad = partes[partes.length - 2].trim();
                            }
                        }
                    }

                    LocalDate fechaInicio = null;
                    LocalDate fechaFin = null;

                    JsonNode fechas = nodo.get("eventario_fecha");

                    if (fechas != null && fechas.size() > 0) {
                        String start = fechas.get(0).get("start").asText();
                        String end = fechas.get(0).get("end").asText();

                        fechaInicio = LocalDate.parse(start.substring(0, 10));
                        fechaFin = LocalDate.parse(end.substring(0, 10));
                    }

                    String categoria = "";

                    JsonNode categorias = nodo.get("event_category_details");

                    if (categorias != null && categorias.size() > 0) {
                        categoria = categorias.get(0).get("name").asText();
                    }

                    boolean esCultura = false;

                    if (categorias != null) {
                        for (JsonNode cat : categorias) {
                            String nombreCat = cat.get("name").asText().toLowerCase();

                            if (nombreCat.contains("cultura")
                                    || nombreCat.contains("arte")
                                    || nombreCat.contains("música")
                                    || nombreCat.contains("musica")) {

                                esCultura = true;
                                break;
                            }
                        }
                    }

                    // if (!esCultura) continue;

                    if (fechaFin != null && fechaFin.isBefore(LocalDate.now())) {
                        continue;
                    }

                    Actividad act = Actividad.builder()
                            .nombre(titulo)
                            .descripcion(descripcion)
                            .imagenUrl(imagen)
                            .fuente("EVENTARIO")
                            .fuenteUrl(link)
                            .categoriaString(categoria)
                            .vigenciaInicio(fechaInicio)
                            .vigenciaFin(fechaFin)
                            .costoPorPersona(0.0)
                            .duracionMin(120)
                            .fechaCreacion(LocalDateTime.now())
                            .ubicacion(new Ubicacion(ciudad, direccion, lat, lon))
                            .build();

                    lista.add(act);
                }

                page++;
            }

            System.out.println("Eventos Eventario: " + lista.size());

        } catch (Exception e) {
            e.printStackTrace();
        }

        return lista;
    }
}