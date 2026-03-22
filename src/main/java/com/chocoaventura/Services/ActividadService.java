package com.chocoaventura.services;

import java.time.LocalDate;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.chocoaventura.repositories.ActividadRepository;
import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Imagen;

@Service
public class ActividadService {

    private final ActividadRepository actividadRepository;

    public ActividadService(ActividadRepository actividadRepository) {
        this.actividadRepository = actividadRepository;
    }

    @Async
    public void actualizarTodo() {
        scrapearIdartes();
        scrapearTuBoleta();
        scrapearBogotaGov();
    }

    public void scrapearIdartes() {
        try {
            int pagina = 0;
            boolean hayMas = true;

            while (hayMas) {
                String url = "https://www.idartes.gov.co/es/agenda?page=" + pagina;
                Document doc = Jsoup.connect(url).userAgent("Mozilla/5.0").timeout(20000).get();
                Elements tarjetas = doc.select(".view-content .views-row");

                if (tarjetas.isEmpty()) {
                    hayMas = false;
                    break;
                }

                for (Element tarjeta : tarjetas) {
                    String linkRelativo = tarjeta.select("a").first().attr("href");
                    String urlDetalle = "https://www.idartes.gov.co" + linkRelativo;

                    Document detalleDoc = Jsoup.connect(urlDetalle).userAgent("Mozilla/5.0").get();
                    String nombre = detalleDoc.select("h1.title").text().trim();

                    if (existeEvento(nombre)) {
                        continue;
                    }

                    String cuerpo = detalleDoc.select(".field-name-body").text();
                    String fechaTexto = detalleDoc.select(".field-name-field-fecha-evento").text();
                    String precios = extraerPreciosDelTexto(cuerpo);
                    String imagenUrl = detalleDoc.select(".field-name-field-imagen-principal img").attr("src");

                    Actividad act = new Actividad(
                            nombre,
                            cuerpo,
                            extraerPrecioMinimo(precios),
                            60 // duración por defecto mientras no tengamos ese dato real
                    );

                    act.setFuente("Idartes");
                    act.setPreciosDetallados(precios.isEmpty() ? "Entrada Libre / Consultar" : precios);
                    act.setVigenciaInicio(parsearFechaEspanol(fechaTexto));

                    if (imagenUrl != null && !imagenUrl.isBlank()) {
                        Imagen imagen = new Imagen(imagenUrl, act);
                        act.getImagenes().add(imagen);
                    }

                    actividadRepository.save(act);
                    System.out.println("🎭 IDARTES Guardado: " + nombre);
                }
                pagina++;
            }
        } catch (Exception e) {
            System.err.println("Error Idartes: " + e.getMessage());
        }
    }

    public void scrapearTuBoleta() {
        try {
            System.out.println("--- INICIANDO SCRAPER MASIVO TUBOLETA ---");
            Document sitemap = Jsoup.connect("https://tuboleta.com/sitemap.xml")
                    .userAgent("Mozilla/5.0")
                    .timeout(30000)
                    .get();
            Elements urls = sitemap.select("loc");

            System.out.println("URLs encontradas en sitemap: " + urls.size());

            for (Element loc : urls) {
                String urlLimpia = loc.text();

                if (urlLimpia.contains("/eventos/") && !urlLimpia.contains("demo") && !urlLimpia.contains("terminos")) {
                    try {
                        Document doc = Jsoup.connect(urlLimpia.replace("prod.", ""))
                                .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64)")
                                .timeout(15000)
                                .get();

                        Element scriptJson = doc.select("script[type=application/ld+json]").first();
                        if (scriptJson == null) {
                            continue;
                        }

                        String json = scriptJson.html();

                        String nombre = doc.select("meta[property=og:title]").attr("content").split("\\|")[0].trim();
                        if (nombre.isEmpty() || existeEvento(nombre)) {
                            continue;
                        }

                        StringBuilder mapaPrecios = new StringBuilder();
                        Double precioMinimo = Double.MAX_VALUE;

                        Pattern pPrices = Pattern.compile("\"name\"\\s*:\\s*\"([^\"]+)\"\\s*,[^}]*\"price\"\\s*:\\s*\"?(\\d+)\"?");
                        Matcher mPrices = pPrices.matcher(json);

                        while (mPrices.find()) {
                            String zona = mPrices.group(1);
                            String valor = mPrices.group(2);

                            if (zona.equalsIgnoreCase("TuBoleta") || zona.length() < 2) {
                                continue;
                            }

                            mapaPrecios.append(zona).append(": $").append(valor).append(" | ");

                            double p = Double.parseDouble(valor);
                            if (p > 0 && p < precioMinimo) {
                                precioMinimo = p;
                            }
                        }

                        String imagenUrl = doc.select("meta[property=og:image]").attr("content");

                        Actividad act = new Actividad(
                                nombre,
                                doc.select("meta[property=og:description]").attr("content"),
                                precioMinimo == Double.MAX_VALUE ? 0.0 : precioMinimo,
                                60 // duración por defecto
                        );

                        act.setPreciosDetallados(
                                mapaPrecios.toString().isEmpty() ? "Consultar en TuBoleta" : mapaPrecios.toString()
                        );
                        act.setVigenciaInicio(extraerFechaJson(json, "startDate"));
                        act.setFuente("TuBoleta");

                        if (imagenUrl != null && !imagenUrl.isBlank()) {
                            Imagen imagen = new Imagen(imagenUrl, act);
                            act.getImagenes().add(imagen);
                        }

                        actividadRepository.save(act);
                        System.out.println("🎟️ TUBOLETA Guardado: " + nombre + " (Min: $" + act.getCostoPorPersona() + ")");

                    } catch (Exception e) {
                        System.err.println("Error procesando evento: " + urlLimpia);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Error crítico en TuBoleta: " + e.getMessage());
        }
    }

    public void scrapearBogotaGov() {
        try {
            Document doc = Jsoup.connect("https://bogota.gov.co/que-hacer/agenda-cultural")
                    .userAgent("Mozilla/5.0").get();
            Elements links = doc.select(".views-field-title a");

            for (Element link : links) {
                String urlDetalle = "https://bogota.gov.co" + link.attr("href");
                Document det = Jsoup.connect(urlDetalle).userAgent("Mozilla/5.0").get();

                String nombre = det.select("h1.is-title").text().trim();
                if (existeEvento(nombre) || nombre.isEmpty()) {
                    continue;
                }

                String infoText = det.select(".field-name-body").text();
                String imagenUrl = det.select("meta[property=og:image]").attr("content");

                Actividad act = new Actividad(
                        nombre,
                        infoText,
                        0.0,
                        60 // duración por defecto
                );

                act.setFuente("Bogotá.gov");
                act.setVigenciaInicio(LocalDate.now());

                if (imagenUrl != null && !imagenUrl.isBlank()) {
                    Imagen imagen = new Imagen(imagenUrl, act);
                    act.getImagenes().add(imagen);
                }

                actividadRepository.save(act);
                System.out.println("🏙️ BOG GOV Guardado: " + nombre);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private LocalDate extraerFechaJson(String json, String campo) {
        try {
            Pattern p = Pattern.compile("\"" + campo + "\":\"(\\d{4}-\\d{2}-\\d{2})");
            Matcher m = p.matcher(json);
            if (m.find()) {
                return LocalDate.parse(m.group(1));
            }
        } catch (Exception e) {
            return LocalDate.now();
        }
        return LocalDate.now();
    }

    private LocalDate parsearFechaEspanol(String texto) {
        try {
            texto = texto.toLowerCase();
            int anio = 2026;
            int dia = 1;

            Matcher mDia = Pattern.compile("(\\d{1,2})").matcher(texto);
            if (mDia.find()) {
                dia = Integer.parseInt(mDia.group(1));
            }

            int mes = 1;
            if (texto.contains("ene")) mes = 1;
            else if (texto.contains("feb")) mes = 2;
            else if (texto.contains("mar")) mes = 3;
            else if (texto.contains("abr")) mes = 4;
            else if (texto.contains("may")) mes = 5;
            else if (texto.contains("jun")) mes = 6;
            else if (texto.contains("jul")) mes = 7;
            else if (texto.contains("ago")) mes = 8;
            else if (texto.contains("sep")) mes = 9;
            else if (texto.contains("oct")) mes = 10;
            else if (texto.contains("nov")) mes = 11;
            else if (texto.contains("dic")) mes = 12;

            return LocalDate.of(anio, mes, dia);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }

    private String extraerPreciosDelTexto(String texto) {
        Pattern p = Pattern.compile("(\\w+)?\\s?:?\\s?\\$\\s?(\\d{1,3}(\\.\\d{3})*)");
        Matcher m = p.matcher(texto);
        StringBuilder sb = new StringBuilder();

        while (m.find()) {
            String zona = m.group(1) != null ? m.group(1) : "General";
            sb.append(zona).append(": $").append(m.group(2)).append(" | ");
        }

        return sb.toString();
    }

    private Double extraerPrecioMinimo(String preciosFormateados) {
        if (preciosFormateados.isEmpty()) {
            return 0.0;
        }

        try {
            return Pattern.compile("\\d+(\\.\\d+)?")
                    .matcher(preciosFormateados.replace(".", ""))
                    .results()
                    .mapToDouble(m -> Double.parseDouble(m.group()))
                    .min()
                    .orElse(0.0);
        } catch (Exception e) {
            return 0.0;
        }
    }

    private boolean existeEvento(String nombre) {
        return actividadRepository.existsByNombreIgnoreCase(nombre);
    }

    public List<Actividad> getAll() {
        return actividadRepository.findAll();
    }
}