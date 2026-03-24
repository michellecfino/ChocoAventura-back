package com.chocoaventura.services;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Ciudad;
import com.chocoaventura.entities.Imagen;
import com.chocoaventura.entities.Ubicacion;
import com.chocoaventura.repositories.ActividadRepository;
import com.chocoaventura.repositories.CiudadRepository;
import com.chocoaventura.repositories.UbicacionRepository;

import jakarta.persistence.EntityNotFoundException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

@Service
public class ActividadService {

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private UbicacionRepository ubicacionRepository;

    @Autowired
    private CiudadRepository ciudadRepository;

    // =========================
    // CRUD básico
    // =========================

    public Actividad create(Actividad actividad) {
        if (actividadRepository.existsByNombreIgnoreCase(actividad.getNombre())) {
            throw new IllegalArgumentException("Ya existe una actividad con ese nombre.");
        }
        return actividadRepository.save(actividad);
    }

    public List<Actividad> getAll() {
        return actividadRepository.findAll();
    }

    public Actividad getById(Long id) {
        return actividadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con id: " + id));
    }

    public Actividad update(Long id, Actividad datos) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con id: " + id));

        actividad.setNombre(datos.getNombre());
        actividad.setDescripcion(datos.getDescripcion());
        actividad.setCostoPorPersona(datos.getCostoPorPersona());
        actividad.setDuracionMin(datos.getDuracionMin());
        actividad.setCalificacionPromedio(datos.getCalificacionPromedio());
        actividad.setVigenciaInicio(datos.getVigenciaInicio());
        actividad.setVigenciaFin(datos.getVigenciaFin());
        actividad.setPreciosDetallados(datos.getPreciosDetallados());
        actividad.setFuente(datos.getFuente());
        actividad.setCiudad(datos.getCiudad());
        actividad.setUbicacion(datos.getUbicacion());
        actividad.setCategorias(datos.getCategorias());

        return actividadRepository.save(actividad);
    }

    public void delete(Long id) {
        Actividad actividad = actividadRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con id: " + id));
        actividadRepository.delete(actividad);
    }

    // =========================
    // Consultas útiles MVP
    // =========================

    public List<Actividad> buscarPorNombre(String texto) {
        return actividadRepository.findByNombreContainingIgnoreCase(texto);
    }

    public List<Actividad> actividadesVigentesDesde(LocalDate fecha) {
        return actividadRepository.findByVigenciaInicioGreaterThanEqual(fecha);
    }

    // =========================
    // Scraping
    // =========================

    @Async
    public void actualizarTodo() {
        try {
            System.out.println("🔄 Iniciando scraping de todas las fuentes...");
            scrapearIdartes();
            scrapearTuBoleta();
            scrapearBogotaGov();
            System.out.println("  Scraping completado exitosamente");
        } catch (Exception e) {
            System.err.println("  Error en scraping general: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void scrapearIdartes() {
        try {
            trustAllCertificates();
            int pagina = 0;
            boolean hayMas = true;
            int totalGuardados = 0;

            while (hayMas && pagina < 10) { 
                String url = "https://www.idartes.gov.co/es/agenda?page=" + pagina;
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(20000)
                        .get();

                Elements eventos = doc.select(".item-ev.position-relative");

                if (eventos.isEmpty()) {
                    hayMas = false;
                    System.out.println("📄 No hay más eventos en la página " + pagina);
                    break;
                }

                System.out.println("📊 Página " + pagina + " - Eventos encontrados: " + eventos.size());

                for (Element evento : eventos) {
                    try {
                        Element contenedor = evento.select(".cajashomeeventos").first();
                        if (contenedor == null)
                            continue;

                        Element nombreElem = contenedor.select(".titulo_cajashomeeventos a").first();
                        String nombre = nombreElem != null ? nombreElem.text().trim() : "";

                        if (nombre.isEmpty() || existeEvento(nombre)) {
                            if (!nombre.isEmpty())
                                System.out.println("   ⏭️ Evento ya existe: " + nombre);
                            continue;
                        }

                        System.out.print("   🔍 Procesando: " + nombre + " ... ");

                        Element catElem = contenedor.select(".ctg-ev-24").first();
                        String categoria = catElem != null ? catElem.text().trim() : "";

                        Element fechaElem = contenedor.select(".fecha_cajashomeeventos .fecha-ev24").first();
                        String fechaTexto = fechaElem != null ? fechaElem.text().trim() : "";
                        LocalDate fecha = parsearFechaEspanol(fechaTexto);
                        String horario = extraerHorario(fechaTexto);

                        Element precioElem = contenedor.select(".tipo_cajashomeeventos").first();
                        Double costoPorPersona = 0.0;
                        Map<String, Double> preciosDetallados = new HashMap<>();

                        if (precioElem != null) {
                            String precioTexto = precioElem.text().trim();
                            if (precioTexto.contains("Entrada libre")) {
                                preciosDetallados.put("Entrada libre", 0.0);
                            } else {
                                Double precioExtraido = extraerPrecioDelTexto(precioTexto);
                                if (precioExtraido > 0) {
                                    costoPorPersona = precioExtraido;
                                    preciosDetallados.put("General", precioExtraido);
                                }
                            }
                        }

                        Element descElem = contenedor.select(".descripcion_cajashomeeventos").first();
                        String descripcion = descElem != null ? descElem.text().trim() : "";

                        Element imgElem = contenedor.select("img").first();
                        String imagenUrl = "";
                        if (imgElem != null) {
                            String src = imgElem.absUrl("src");
                            if (src != null && !src.isEmpty()) {
                                imagenUrl = src;
                            }
                        }

                        String enlace = nombreElem != null ? nombreElem.absUrl("href") : "";

                        // Crear actividad
                        Actividad act = new Actividad(nombre, descripcion, costoPorPersona, 90);
                        act.setImagenUrl(imagenUrl);
                        act.setCalificacionPromedio(0.0);
                        act.setVigenciaInicio(fecha);
                        act.setVigenciaFin(fecha);
                        act.setPreciosDetallados(preciosDetallados);
                        act.setFuente("Idartes");
                        act.setCiudad(null);
                        act.setUbicacion(null);

                        if (!imagenUrl.isEmpty()) {
                            Imagen imagen = new Imagen(imagenUrl, act);
                            imagen.setEsPrincipal(true);
                            act.getImagenes().add(imagen);
                        }

                        actividadRepository.save(act);
                        totalGuardados++;
                        System.out.println("   Guardado");

                    } catch (Exception e) {
                        System.err.println(" Error procesando evento: " + e.getMessage());
                    }
                }
                pagina++;
                Thread.sleep(500);
            }

            System.out.println(
                    "\n Scraping de Idartes completado. Se guardaron " + totalGuardados + " actividades nuevas.");

        } catch (Exception e) {
            System.err.println("  Error crítico en Idartes: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void trustAllCertificates() {
        try {
            TrustManager[] trustAllCerts = new TrustManager[] {
                    new X509TrustManager() {
                        public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                            return null;
                        }

                        public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }

                        public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                        }
                    }
            };
            SSLContext sc = SSLContext.getInstance("TLS");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier((hostname, session) -> true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private String extraerHorario(String fechaStr) {
        Pattern pattern = Pattern.compile("\\d{1,2}:\\d{2}\\s*(am|pm)");
        Matcher matcher = pattern.matcher(fechaStr);
        if (matcher.find()) {
            return matcher.group();
        }
        return "";
    }

    private Double extraerPrecioDelTexto(String texto) {
        Pattern pattern = Pattern.compile("\\$(\\d{1,3}(?:\\.\\d{3})*)");
        Matcher matcher = pattern.matcher(texto);
        if (matcher.find()) {
            try {
                String numStr = matcher.group(1).replace(".", "");
                return Double.parseDouble(numStr);
            } catch (NumberFormatException e) {
                return 0.0;
            }
        }
        return 0.0;
    }

    public void scrapearTuBoleta() {
        try {
            String url = "https://tuboleta.com/es/resultados-de-busqueda?ciudades=All&categorias=All";

            Document doc = Jsoup.connect(url)
                    .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                    .timeout(15000)
                    .get();

            System.out.println("  Conectando a Tuboleta: " + doc.title());

            Elements tarjetas = doc.select("article.p-3.h-100.bg-grey-light.rounded-1.position-relative");
            System.out.println("📊 Eventos encontrados en primera página: " + tarjetas.size());

            List<Actividad> actividadesAGuardar = new ArrayList<>();
            int totalProcesados = 0;

            for (Element tarjeta : tarjetas) {
                try {
                    EventoTuboleta evento = new EventoTuboleta();

                    Element nombreElem = tarjeta.select(".content-info .fs-8.fs-md-7.text-uppercase.fw-bold.mb-1")
                            .first();
                    if (nombreElem != null) {
                        evento.nombre = nombreElem.text().trim();
                    }

                    if (evento.nombre.isEmpty()) {
                        continue;
                    }

                    if (existeEvento(evento.nombre)) {
                        System.out.println("   ⏭️ Evento ya existe: " + evento.nombre);
                        continue;
                    }

                    Elements lugarCiudad = tarjeta.select(".content-info .fs-8.fs-md-7.text-grey");
                    if (lugarCiudad.size() >= 1) {
                        evento.lugar = lugarCiudad.get(0).text().trim();
                    }
                    if (lugarCiudad.size() >= 2) {
                        evento.ciudad = lugarCiudad.get(1).text().trim();
                    }

                    Elements fechas = tarjeta.select(".dates-container .content-date");
                    if (fechas.size() == 1) {
                        evento.fechaUnica = fechas.first().text().trim();
                    } else if (fechas.size() == 2) {
                        evento.fechaDesde = fechas.first().text().replace("Desde", "").trim();
                        evento.fechaHasta = fechas.last().text().replace("Hasta", "").trim();
                    }

                    Element imgElem = tarjeta.select(".image-container picture img").first();
                    if (imgElem != null) {
                        String src = imgElem.absUrl("src");
                        if (src != null && !src.isEmpty()) {
                            evento.imagenUrl = src;
                        }
                    }

                    Element linkElem = tarjeta.select("a.content-link-container").first();
                    if (linkElem != null) {
                        String href = linkElem.attr("href");
                        if (href != null && !href.isEmpty()) {
                            evento.enlace = href;
                        }
                    }

                    if (evento.lugar.isEmpty()) {
                        System.out.println("   ⚠️ Evento sin lugar, omitiendo: " + evento.nombre);
                        continue;
                    }

                    String nombreLower = evento.nombre.toLowerCase();
                    if (nombreLower.contains("asistencia tuboleta") ||
                            nombreLower.contains("bono regalo") ||
                            nombreLower.contains("pasala tuboleta") ||
                            nombreLower.contains("plan separa") ||
                            nombreLower.contains("tuboleta te lleva") ||
                            nombreLower.contains("tuboleta te cree")) {
                        System.out.println("   ⚠️ Filtrado servicio: " + evento.nombre);
                        continue;
                    }

                    System.out.print("   🔍 Procesando: " + evento.nombre + " ... ");

                    Document detalle = Jsoup.connect("https://tuboleta.com" + evento.enlace)
                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                            .timeout(15000)
                            .get();

                    evento.descripcion = extraerDescripcionTuboleta(detalle);

                    evento.precios = extraerPreciosDeDocumentoTuboleta(detalle);

                    if (evento.precios.isEmpty()) {
                        Elements posiblesHubs = detalle
                                .select(".featured-events .field__item article, .field__items .field__item article");
                        if (!posiblesHubs.isEmpty()) {
                            System.out.print(" (hub detectado)");
                            Element primerEvento = posiblesHubs.first();
                            Element enlaceEvento = primerEvento.select("a.content-link-container, a").first();
                            if (enlaceEvento != null && enlaceEvento.hasAttr("href")) {
                                String subEnlace = enlaceEvento.attr("href");
                                if (subEnlace != null && !subEnlace.isEmpty()) {
                                    Document subDetalle = Jsoup.connect("https://tuboleta.com" + subEnlace)
                                            .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                                            .timeout(15000)
                                            .get();

                                    evento.precios = extraerPreciosDeDocumentoTuboleta(subDetalle);
                                    String descSub = extraerDescripcionTuboleta(subDetalle);
                                    if (!descSub.isEmpty()) {
                                        evento.descripcion = descSub;
                                    }

                                    Element nombreSub = subDetalle.select("h1").first();
                                    if (nombreSub != null) {
                                        evento.nombre = nombreSub.text().trim();
                                    }

                                    Elements lugarCiudadSub = subDetalle
                                            .select(".content-info .fs-8.fs-md-7.text-grey");
                                    if (lugarCiudadSub.size() >= 1) {
                                        evento.lugar = lugarCiudadSub.get(0).text().trim();
                                    }
                                    if (lugarCiudadSub.size() >= 2) {
                                        evento.ciudad = lugarCiudadSub.get(1).text().trim();
                                    }

                                    Elements fechasSub = subDetalle.select(".dates-container .content-date");
                                    if (fechasSub.size() == 1) {
                                        evento.fechaUnica = fechasSub.first().text().trim();
                                        evento.fechaDesde = "";
                                        evento.fechaHasta = "";
                                    } else if (fechasSub.size() == 2) {
                                        evento.fechaDesde = fechasSub.first().text().replace("Desde", "").trim();
                                        evento.fechaHasta = fechasSub.last().text().replace("Hasta", "").trim();
                                        evento.fechaUnica = "";
                                    }
                                }
                            }
                        }
                    }

                    Actividad actividad = convertirEventoAActividad(evento);

                    actividadesAGuardar.add(actividad);
                    totalProcesados++;
                    System.out.println("   Guardado (precios: " + evento.precios.size() + ")");

                    Thread.sleep(500);

                } catch (Exception e) {
                    System.err.println("  Error procesando evento: " + e.getMessage());
                }
            }

            if (!actividadesAGuardar.isEmpty()) {
                actividadRepository.saveAll(actividadesAGuardar);
                System.out.println("\n  Se guardaron " + actividadesAGuardar.size() + " actividades de Tuboleta");
            } else {
                System.out.println("\n⚠️ No se encontraron nuevas actividades en Tuboleta");
            }

        } catch (Exception e) {
            System.err.println("  Error crítico en Tuboleta: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =========================
    // Métodos auxiliares para Tuboleta
    // =========================

    private String extraerDescripcionTuboleta(Document doc) {
    Element metaDesc = doc.select("meta[name=description]").first();
    if (metaDesc != null) {
        String desc = metaDesc.attr("content");
        if (!desc.isEmpty() && !desc.contains("tiquetera") && desc.length() > 50) {
            String[] partes = desc.split(",");
            if (partes.length > 0 && partes[0].length() > 30) {
                desc = partes[0].trim();
            }
            if (desc.length() > 500) desc = desc.substring(0, 500) + "...";
            return desc;
        }
    }
    
    String[] selectores = {
        ".field--name-body p",
        ".descripcion_cajashomeeventos",
        ".event-description",
        ".node__content .field--type-text-long",
        ".description",
        ".event-info p"
    };
    
    for (String selector : selectores) {
        Elements elementos = doc.select(selector);
        for (Element elem : elementos) {
            String texto = elem.text().trim();
            if (!texto.isEmpty() && 
                texto.length() > 30 &&
                !texto.contains("Somos la tiquetera") &&
                !texto.contains("Ticket Fast S.A.S") &&
                !texto.contains("Call Center") &&
                !texto.contains("Nit") &&
                !texto.contains("© 2026 Tuboleta")) {
                if (texto.length() > 500) texto = texto.substring(0, 500) + "...";
                return texto;
            }
        }
    }
    
    Element mainContent = doc.select("main .content, .node__content, article").first();
    if (mainContent != null) {
        Elements parrafos = mainContent.select("p");
        for (Element p : parrafos) {
            String texto = p.text().trim();
            if (texto.length() > 40 && 
                !texto.contains("tiquetera") &&
                !texto.contains("Tuboleta") &&
                texto.length() < 800) {
                return texto.substring(0, Math.min(500, texto.length()));
            }
        }
    }
    
    Element titleElem = doc.select("h1").first();
    if (titleElem != null) {
        return "Evento: " + titleElem.text().trim();
    }
    
    return "Sin descripción disponible";
}

    private Map<String, String> extraerPreciosDeDocumentoTuboleta(Document doc) {
        Map<String, String> preciosMap = new HashMap<>();
        Element tabla = null;

        Elements h2s = doc.select("h2");
        for (Element h2 : h2s) {
            if (Pattern.compile("ubicación y precio", Pattern.CASE_INSENSITIVE).matcher(h2.text()).find()) {
                Element contenedor = h2.parent();
                tabla = contenedor.select("table").first();
                if (tabla == null) {
                    Element siguiente = contenedor.nextElementSibling();
                    if (siguiente != null)
                        tabla = siguiente.select("table").first();
                }
                if (tabla != null)
                    break;
            }
        }

        if (tabla == null) {
            Elements tablas = doc.select("table");
            for (Element t : tablas) {
                Elements ths = t.select("th");
                for (Element th : ths) {
                    if (Pattern.compile("ubicación", Pattern.CASE_INSENSITIVE).matcher(th.text()).find()) {
                        tabla = t;
                        break;
                    }
                }
                if (tabla != null)
                    break;
            }
        }

        if (tabla != null) {
            Elements filas = tabla.select("tbody tr");
            for (Element fila : filas) {
                Element ubicacionElem = fila.select("th").first();
                Element precioElem = fila.select("td").first();
                if (ubicacionElem != null && precioElem != null) {
                    String ubicacion = ubicacionElem.text().trim();
                    String precio = precioElem.text().trim().replaceAll("\\s+", " ").trim();
                    if (!ubicacion.isEmpty() && !precio.isEmpty() && precio.length() < 200) {
                        preciosMap.put(ubicacion, precio);
                    }
                }
            }
        }
        return preciosMap;
    }

    private Actividad convertirEventoAActividad(EventoTuboleta evento) {
        Double precioMinimo = extraerPrecioMinimoTuboleta(evento.precios);

        Integer duracion = estimarDuracionTuboleta(evento.nombre);

        Actividad actividad = new Actividad(
                evento.nombre,
                evento.descripcion.isEmpty() ? "Sin descripción disponible" : evento.descripcion,
                precioMinimo,
                duracion);

        actividad.setImagenUrl(evento.imagenUrl);
        actividad.setCalificacionPromedio(0.0);
        actividad.setFuente("Tuboleta");

        Map<String, Double> preciosDetallados = new HashMap<>();
        Pattern pattern = Pattern.compile("\\$(\\d{1,3}(?:\\.\\d{3})*)");
        for (Map.Entry<String, String> entry : evento.precios.entrySet()) {
            Matcher matcher = pattern.matcher(entry.getValue());
            if (matcher.find()) {
                try {
                    String numStr = matcher.group(1).replace(".", "");
                    double valor = Double.parseDouble(numStr);
                    preciosDetallados.put(entry.getKey(), valor);
                } catch (NumberFormatException e) {
                }
            }
        }
        actividad.setPreciosDetallados(preciosDetallados);

        if (!evento.fechaUnica.isEmpty()) {
            LocalDate fecha = parsearFechaEspanol(evento.fechaUnica);
            actividad.setVigenciaInicio(fecha);
            actividad.setVigenciaFin(fecha);
        } else if (!evento.fechaDesde.isEmpty() && !evento.fechaHasta.isEmpty()) {
            actividad.setVigenciaInicio(parsearFechaEspanol(evento.fechaDesde));
            actividad.setVigenciaFin(parsearFechaEspanol(evento.fechaHasta));
        } else {
            actividad.setVigenciaInicio(LocalDate.now());
            actividad.setVigenciaFin(LocalDate.now().plusMonths(1));
        }

        return actividad;
    }

    private Double extraerPrecioMinimoTuboleta(Map<String, String> precios) {
        if (precios.isEmpty())
            return 0.0;

        double min = Double.MAX_VALUE;
        Pattern pattern = Pattern.compile("\\$(\\d{1,3}(?:\\.\\d{3})*)");

        for (String precioStr : precios.values()) {
            Matcher matcher = pattern.matcher(precioStr);
            while (matcher.find()) {
                try {
                    String numStr = matcher.group(1).replace(".", "");
                    double valor = Double.parseDouble(numStr);
                    if (valor < min) {
                        min = valor;
                    }
                } catch (NumberFormatException e) {
                }
            }
        }

        return min != Double.MAX_VALUE ? min : 0.0;
    }

    private Integer estimarDuracionTuboleta(String nombre) {
        String nombreLower = nombre.toLowerCase();
        if (nombreLower.contains("concierto") || nombreLower.contains("tour"))
            return 180;
        if (nombreLower.contains("teatro") || nombreLower.contains("obra"))
            return 120;
        if (nombreLower.contains("planetario") || nombreLower.contains("fulldome"))
            return 60;
        if (nombreLower.contains("exposición") || nombreLower.contains("museo"))
            return 90;
        return 120;
    }

    public void scrapearBogotaGov() {
        try {
            Document doc = Jsoup.connect("https://bogota.gov.co/que-hacer/agenda-cultural").userAgent("Mozilla/5.0")
                    .get();
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

                Actividad act = new Actividad(nombre, infoText, 0.0, 60);

                act.setFuente("Bogotá.gov");
                act.setVigenciaInicio(LocalDate.now());

                if (imagenUrl != null && !imagenUrl.isBlank()) {
                    Imagen imagen = new Imagen(imagenUrl, act);
                    act.getImagenes().add(imagen);
                }

                actividadRepository.save(act);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // =========================
    // Clase interna para almacenar datos de Tuboleta jiji
    // =========================
    private static class EventoTuboleta {
        String nombre = "";
        String descripcion = "";
        String lugar = "";
        String ciudad = "";
        String fechaDesde = "";
        String fechaHasta = "";
        String fechaUnica = "";
        String imagenUrl = "";
        String enlace = "";
        Map<String, String> precios = new HashMap<>();
    }

    // =========================
    // Métodos auxiliares generales
    // =========================


    private LocalDate parsearFechaEspanol(String texto) {
        try {
            texto = texto.toLowerCase();
            int anio = LocalDate.now().getYear();
            int dia = 1;

            Matcher mDia = Pattern.compile("(\\d{1,2})").matcher(texto);
            if (mDia.find()) {
                dia = Integer.parseInt(mDia.group(1));
            }

            int mes = 1;
            if (texto.contains("ene"))
                mes = 1;
            else if (texto.contains("feb"))
                mes = 2;
            else if (texto.contains("mar"))
                mes = 3;
            else if (texto.contains("abr"))
                mes = 4;
            else if (texto.contains("may"))
                mes = 5;
            else if (texto.contains("jun"))
                mes = 6;
            else if (texto.contains("jul"))
                mes = 7;
            else if (texto.contains("ago"))
                mes = 8;
            else if (texto.contains("sep"))
                mes = 9;
            else if (texto.contains("oct"))
                mes = 10;
            else if (texto.contains("nov"))
                mes = 11;
            else if (texto.contains("dic"))
                mes = 12;

            if (mes < LocalDate.now().getMonthValue() && mes == 1) {
                anio++;
            }

            return LocalDate.of(anio, mes, dia);
        } catch (Exception e) {
            return LocalDate.now();
        }
    }


    private boolean existeEvento(String nombre) {
        return actividadRepository.existsByNombreIgnoreCase(nombre);
    }

    //método para borrar todooooo
    public void deleteAll() {
        actividadRepository.deleteAll();
    }
}