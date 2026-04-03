package com.chocoaventura.services;

import com.chocoaventura.DTOs.ActividadResponseDTO;
import com.chocoaventura.DTOs.CiudadResponseDTO;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.LocalDate;
import java.time.LocalDateTime;
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

    private final ObjectMapper objectMapper = new ObjectMapper();

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

    public List<ActividadResponseDTO> getAllDTOs() {
        List<Actividad> actividades = actividadRepository.findAll();
        return actividades.stream().map(this::mapToDTO).toList();
    }

    public ActividadResponseDTO mapToDTO(Actividad actividad) {
        // Mapear manualmente
        CiudadResponseDTO ciudadDTO = actividad.getCiudad() != null ? new CiudadResponseDTO(
            actividad.getCiudad().getId(),
            actividad.getCiudad().getNombre(),
            actividad.getCiudad().getPais()
        ) : null;

        String preciosJson = null;
        try {
            preciosJson = objectMapper.writeValueAsString(actividad.getPreciosDetallados());
        } catch (JsonProcessingException e) {
            preciosJson = "{}";
        }

        Double latitud = actividad.getUbicacion() != null ? actividad.getUbicacion().getLatitud() : null;
        Double longitud = actividad.getUbicacion() != null ? actividad.getUbicacion().getLongitud() : null;

        return new ActividadResponseDTO(
            actividad.getId(),
            actividad.getNombre(),
            actividad.getDescripcion(),
            actividad.getCostoPorPersona(),
            actividad.getDuracionMin(),
            actividad.getCalificacionPromedio(),
            actividad.getVigenciaInicio(),
            actividad.getVigenciaFin(),
            preciosJson,
            actividad.getFuente(),
            actividad.getFuenteUrl(),
            actividad.getImagenUrl(),
            actividad.getCategoriaString(),
            latitud,
            longitud,
            actividad.getFechaCreacion(),
            ciudadDTO,
            null, // ubicacion
            null, // categorias
            null  // imagenes
        );
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

    private boolean existeEvento(String nombre) {
        return actividadRepository.existsByNombreIgnoreCase(nombre);
    }

    @Async
    public void actualizarTodo() {
        try {
            System.out.println("🔄 Iniciando scraping de todas las fuentes...");
            scrapearIdartes();
            System.out.println("  Scraping completado exitosamente");
        } catch (Exception e) {
            System.err.println("  Error en scraping general: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void scrapearIdartes() {
        try {
            trustAllCertificates();
            Ciudad bogota = ciudadRepository.findByNombreIgnoreCase("Bogotá").stream().findFirst().orElse(null);
            if (bogota == null) {
                bogota = new Ciudad();
                bogota.setNombre("Bogotá");
                bogota.setPais("Colombia");
                bogota = ciudadRepository.save(bogota);
            }
            int pagina = 0;
            boolean hayMas = true;
            int totalGuardados = 0;

            while (hayMas && pagina < 10) { 
                String url = "https://www.idartes.gov.co/agenda?page=" + pagina;
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36")
                        .timeout(20000)
                        .get();

                System.out.println("Conectado a: " + doc.title());

                Elements eventos = doc.select(".view-content .views-row");

                System.out.println("Eventos encontrados: " + eventos.size());

                if (eventos.isEmpty()) {
                    hayMas = false;
                    System.out.println("📄 No hay más eventos en la página " + pagina);
                    break;
                }

                System.out.println("📊 Página " + pagina + " - Eventos encontrados: " + eventos.size());

                for (Element evento : eventos) {
                    try {
                        Element nombreElem = evento.select(".views-field-title .field-content a").first();
                        String nombre = nombreElem != null ? nombreElem.text().trim() : "";

                        if (nombre.isEmpty() || existeEvento(nombre)) {
                            if (!nombre.isEmpty())
                                System.out.println("   ⏭️ Evento ya existe: " + nombre);
                            continue;
                        }

                        System.out.print("   🔍 Procesando: " + nombre + " ... ");

                        Element fechaElem = evento.select(".views-field-field-fecha .field-content").first();
                        String fechaTexto = fechaElem != null ? fechaElem.text().trim() : "";
                        LocalDate fecha = parsearFechaEspanol(fechaTexto);
                        String horario = extraerHorario(fechaTexto);

                        Element lugarElem = evento.select(".views-field-field-lugar .field-content").first();
                        String lugar = lugarElem != null ? lugarElem.text().trim() : "";

                        Element descElem = evento.select(".views-field-field-descripcion .field-content").first();
                        String descripcion = descElem != null ? descElem.text().trim() : "";

                        Element precioElem = evento.select(".views-field-field-costo .field-content").first();
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

                        Element imgElem = evento.select(".views-field-field-imagen .field-content img").first();
                        String imagenUrl = "";
                        if (imgElem != null) {
                            String src = imgElem.absUrl("src");
                            if (src != null && !src.isEmpty()) {
                                imagenUrl = src;
                            }
                        }

                        String fuenteUrl = nombreElem != null ? nombreElem.absUrl("href") : "";

                        // categoria and location from detail
                        String categoria = "";
                        if (!fuenteUrl.isEmpty()) {
                            try {
                                Document detalle = Jsoup.connect(fuenteUrl).userAgent("Mozilla/5.0").timeout(15000).get();
                                // categoria
                                Element catElem = detalle.select(".field-name-field-categoria .field-item, .categoria, .field--name-field-categoria").first();
                                if (catElem != null) categoria = catElem.text().trim();
                                // locality
                                Element localityElem = detalle.select(".field-name-field-locality .field-item, .locality").first();
                                if (localityElem != null) {
                                    String loc = localityElem.text().trim();
                                    if (!loc.isEmpty() && !lugar.contains(loc)) lugar += ", " + loc;
                                }
                                // neighborhood
                                Element neighborhoodElem = detalle.select(".field-name-field-neighborhood .field-item, .neighborhood").first();
                                if (neighborhoodElem != null) {
                                    String neigh = neighborhoodElem.text().trim();
                                    if (!neigh.isEmpty() && !lugar.contains(neigh)) lugar += ", " + neigh;
                                }
                            } catch (Exception e) {
                                System.err.println("Error scraping detail: " + e.getMessage());
                            }
                        }

                        // ubicacion
                        Ubicacion ubicacion = null;
                        if (!lugar.isEmpty()) {
                            ubicacion = ubicacionRepository.save(new Ubicacion(lugar, lugar, 4.7110, -74.0721));
                        }

                        // actividad
                        Actividad act = new Actividad(nombre, descripcion, costoPorPersona, 90);
                        act.setImagenUrl(imagenUrl);
                        act.setCalificacionPromedio(0.0);
                        act.setVigenciaInicio(fecha);
                        act.setVigenciaFin(fecha);
                        act.setPreciosDetallados(preciosDetallados);
                        act.setFuente("Idartes");
                        act.setFuenteUrl(fuenteUrl);
                        act.setCategoriaString(categoria);
                        act.setFechaCreacion(LocalDateTime.now());
                        act.setCiudad(bogota);
                        act.setUbicacion(ubicacion);

                        if (!imagenUrl.isEmpty()) {
                            Imagen imagen = new Imagen(imagenUrl, act);
                            imagen.setEsPrincipal(true);
                            act.getImagenes().add(imagen);
                        }

                        actividadRepository.save(act);
                        totalGuardados++;
                        System.out.println("   ✅ Guardado");

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



    // =========================
    // Métodos auxiliares para Tuboleta
    // =========================





    // =========================
    // Clase interna para almacenar datos de Tuboleta jiji
    // =========================


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

    //método para borrar todooooo
    public void deleteAll() {
        actividadRepository.deleteAll();
    }
}