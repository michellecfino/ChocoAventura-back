package com.chocoaventura.services.scrapers;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Ubicacion;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import javax.net.ssl.*;
import java.security.cert.X509Certificate;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ScraperBogota implements Scraper {

    @Override
    public List<Actividad> scrapear() {

        enableSSLIgnore();

        String urlPrincipal = "https://www.culturarecreacionydeporte.gov.co/es/eventos";
        List<Actividad> listaFinal = new ArrayList<>();

        try {
            System.out.println("🚀 Conectando...");

            Document docPrincipal = Jsoup.connect(urlPrincipal)
                    .userAgent("Mozilla/5.0")
                    .timeout(30000)
                    .get();

            Elements links = docPrincipal.select("a[href^=/es/eventos/]");

            for (Element link : links) {
                String urlDetalle = link.attr("abs:href");

                if (urlDetalle.isEmpty() ||
                        listaFinal.stream().anyMatch(a -> a.getFuenteUrl().equals(urlDetalle))) {
                    continue;
                }

                System.out.println("🌐 " + urlDetalle);

                try {
                    Document docInterno = Jsoup.connect(urlDetalle)
                            .userAgent("Mozilla/5.0")
                            .timeout(10000)
                            .get();

                    Element ogTitleEl = docInterno.selectFirst("meta[property=og:title]");
                    String ogTitle = (ogTitleEl != null) ? ogTitleEl.attr("content").trim() : "";

                    String htmlTitle = docInterno.title().trim();

                    Element h1El = docInterno.selectFirst("h1.page-title");
                    String h1Title = (h1El != null) ? h1El.text().trim() : "";

                    String titulo;

                    if (!htmlTitle.isEmpty() && htmlTitle.length() < ogTitle.length()) {
                        titulo = htmlTitle;
                    } else if (!h1Title.isEmpty() && h1Title.length() < ogTitle.length()) {
                        titulo = h1Title;
                    } else if (!ogTitle.isEmpty()) {
                        titulo = ogTitle;
                    } else {
                        titulo = htmlTitle;
                    }

                    Element metaDescEl = docInterno.selectFirst("meta[name=description]");
                    String descripcionCorta = (metaDescEl != null)
                            ? metaDescEl.attr("content").trim()
                            : "";

                    String imagen = docInterno
                            .select("meta[property=og:image]")
                            .attr("content");

                    String urlCanonical = docInterno
                            .select("link[rel=canonical]")
                            .attr("href");

                    String fuenteFinal = (urlCanonical != null && !urlCanonical.isEmpty())
                            ? urlCanonical
                            : urlDetalle;

                    Element contenedor = docInterno.selectFirst("div.field--name-body .field__item");
                    String descripcion = "";

                    if (contenedor != null) {
                        StringBuilder sb = new StringBuilder();

                        for (Element el : contenedor.children()) {
                            if (el.tagName().equals("h2") &&
                                    el.text().toLowerCase().contains("información")) {
                                break;
                            }
                            sb.append(el.text()).append(" ");
                        }

                        descripcion = sb.toString().trim();
                    }

                    if (descripcion.isEmpty()) {
                        descripcion = "Sin descripción disponible";
                    }

                    Elements dias = docInterno.select(".fecha-dia");
                    Elements meses = docInterno.select(".fecha-mes");

                    LocalDate fechaInicio = null;
                    LocalDate fechaFin = null;
                    int anio = LocalDate.now().getYear();

                    try {
                        if (dias.size() >= 1 && meses.size() >= 1) {

                            int diaInicio = Integer.parseInt(dias.get(0).text().trim());
                            int mesInicio = convertirMes(meses.get(0).text().trim().toLowerCase());

                            fechaInicio = LocalDate.of(anio, mesInicio, diaInicio);

                            if (dias.size() >= 2 && meses.size() >= 2) {
                                int diaFin = Integer.parseInt(dias.get(1).text().trim());
                                int mesFin = convertirMes(meses.get(1).text().trim().toLowerCase());

                                fechaFin = LocalDate.of(anio, mesFin, diaFin);
                            } else {
                                fechaFin = fechaInicio;
                            }
                        }
                    } catch (Exception e) {
                        System.err.println("⚠️ Error parseando fechas");
                    }

                    Element lugarEl = docInterno.selectFirst("li:has(strong:contains(Escenario))");

                    String lugar = "";
                    if (lugarEl != null) {
                        lugar = lugarEl.text()
                                .replace("Escenario:", "")
                                .trim();
                    }

                    LocalDate hoy = LocalDate.now();
                    if (fechaFin != null && fechaFin.isBefore(hoy)) {
                        continue;
                    }

                    Actividad act = Actividad.builder()
                            .nombre(titulo)
                            .descripcion(descripcionCorta.isEmpty() ? descripcion : descripcionCorta)
                            .fuenteUrl(fuenteFinal)
                            .ubicacion(new Ubicacion("Bogotá", lugar, 0.0, 0.0))
                            .vigenciaInicio(fechaInicio)
                            .vigenciaFin(fechaFin)
                            .costoPorPersona(0.0)
                            .duracionMin(120)
                            .imagenUrl(imagen)
                            .build();

                    listaFinal.add(act);

                    Thread.sleep(500);

                } catch (Exception e) {
                    System.err.println("Error en detalle: " + urlDetalle);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return listaFinal;
    }

    private static int convertirMes(String mes) {
        switch (mes) {
            case "enero": return 1;
            case "febrero": return 2;
            case "marzo": return 3;
            case "abril": return 4;
            case "mayo": return 5;
            case "junio": return 6;
            case "julio": return 7;
            case "agosto": return 8;
            case "septiembre": return 9;
            case "octubre": return 10;
            case "noviembre": return 11;
            case "diciembre": return 12;
            default: return 1;
        }
    }

    private static void enableSSLIgnore() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[]{
                    new X509TrustManager() {
                        public X509Certificate[] getAcceptedIssuers() { return null; }
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());

            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);

        } catch (Exception ignored) {}
    }
}