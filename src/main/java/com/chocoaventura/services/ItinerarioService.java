package com.chocoaventura.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.AsignacionTokens;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Itinerario;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.RondaSubasta;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.ItinerarioRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class ItinerarioService {

    @Autowired
    private ItinerarioRepository itinerarioRepository;

    @Autowired
    private ItemItinerarioService itemService;

    @Autowired
    private GrupoViajeRepository grupoViajeRepository;

    // =========================
    // CRUD básico
    // =========================

    public Itinerario create(Itinerario itinerario) {
        return itinerarioRepository.save(itinerario);
    }

    public List<Itinerario> getAll() {
        return itinerarioRepository.findAll();
    }

    public Itinerario getById(Long id) {
        return itinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Itinerario no encontrado con id: " + id));
    }

    public Itinerario update(Long id, Itinerario datos) {
        Itinerario itinerario = itinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Itinerario no encontrado con id: " + id));

        itinerario.setNombre(datos.getNombre());
        itinerario.setPresupuestoPromedioPersona(datos.getPresupuestoPromedioPersona());
        itinerario.setGrupoViaje(datos.getGrupoViaje());

        return itinerarioRepository.save(itinerario);
    }

    public void delete(Long id) {
        Itinerario itinerario = itinerarioRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Itinerario no encontrado con id: " + id));
        itinerarioRepository.delete(itinerario);
    }

    // =========================
    // Lógica
    // =========================

    public Itinerario crearItinerario(String nombre, Long grupoViajeId) {
        GrupoViaje grupoViaje = grupoViajeRepository.findById(grupoViajeId).orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + grupoViajeId));
        double [] datos=obtenerPresupuestoHoraPromedio(grupoViaje);
        List<Item> utilidades= puntosPorActividad(grupoViaje);
        List<Actividad> actividadesSeleccionadas= knapsack2D(utilidades, datos, grupoViaje);
        
        Itinerario itinerario = new Itinerario(nombre, datos[0], grupoViaje);
        int maxMinutos= (int) Math.round(datos[1] * 60);
        generarItinerarioOptimizado(grupoViaje, itinerario, actividadesSeleccionadas, maxMinutos);
        grupoViajeRepository.save(grupoViaje);
        return itinerarioRepository.save(itinerario);
    }

    public double[] obtenerPresupuestoHoraPromedio(GrupoViaje grupoViaje){
        double[] respuesta= new double[2];
        respuesta[0]= (double)Integer.MAX_VALUE;
        int totalUsuarios=0;
        double horasTotales=0;
        // presupuesto [0], horas por día actividad [1]
        for(Perfil viajero: grupoViaje.getPerfiles()){
            if (viajero.getPresupuesto()<respuesta[0]) respuesta[0]=viajero.getPresupuesto();
            horasTotales+= viajero.getTiempoDiarioActividades();
            totalUsuarios++;
        }
        respuesta[1]= horasTotales/totalUsuarios;
        return respuesta;
    }

    public List<Item> puntosPorActividad(GrupoViaje grupoViaje){
        HashMap<Actividad, Integer> respuesta= new HashMap<>();
        for (RondaSubasta ronda: grupoViaje.getRondasSubasta()){
            for(AsignacionTokens voto: ronda.getAsignacionesTokens()){
                if (!respuesta.containsKey(voto.getActividad())){
                    respuesta.put(voto.getActividad(), voto.getTokensAsignados());
                }
                else {
                    int actual = respuesta.get(voto.getActividad());
                    respuesta.put(voto.getActividad(), actual + voto.getTokensAsignados());
                }
            }
        }
        List<Item> items = new ArrayList<>();
        for (Map.Entry<Actividad, Integer> entry : respuesta.entrySet()) {
            Actividad act= entry.getKey();
            int utilidad= entry.getValue();
            int tiempo= act.getDuracionMin(); 
            double costo= act.getCostoPorPersona();
            items.add(new Item(act, utilidad, costo, tiempo));
        }
        return items;
    }

    public List<Actividad> knapsack2D(
        List<Item> items,
        double[] datos, 
        GrupoViaje grupoViaje
    ) {
    int n= items.size();
    // DP
    int maxCosto= (int)Math.round(datos[0] * 10);
    int maxMinutos= (int) Math.round(datos[1] * 60);
    int maxTiempo= maxMinutos* calcularDiasActividades(grupoViaje);
    int[][][] dp= new int[n + 1][maxTiempo + 1][maxCosto + 1];

    // Llenado DP
    for (int i = 1; i <= n; i++) {
        Item item = items.get(i - 1);

        for (int t=0; t <= maxTiempo; t++) {
            for (int c=0; c <= maxCosto; c++) {

                if (item.tiempo <= t && item.costo <= c) {
                    dp[i][t][c] = Math.max(
                        dp[i - 1][t][c],
                        dp[i - 1][t - item.tiempo][c - item.costo] + item.utilidad
                    );
                } else {
                    dp[i][t][c] = dp[i - 1][t][c];
                }
            }
        }
    }

    // RECUPERACIÓN
    List<Actividad> seleccionadas = new ArrayList<>();

    int t= maxTiempo;
    int c= maxCosto;

    for (int i = n; i > 0; i--) {
        if (dp[i][t][c] != dp[i - 1][t][c]) {
            Item item = items.get(i - 1);

            seleccionadas.add(item.actividad);

            t -= item.tiempo;
            c -= item.costo;
        }
    }

    return seleccionadas;
    }

    public int calcularDiasActividades(GrupoViaje grupoViaje) {
    LocalDate llegada = grupoViaje.getFechaHoraLlegada().toLocalDate();
    LocalDate salida = grupoViaje.getFechaHoraSalida().toLocalDate();
    int dias = (int) ChronoUnit.DAYS.between(llegada, salida);
    if (grupoViaje.getFechaHoraLlegada().toLocalTime().isAfter(grupoViaje.getHoraInicioActividades())) {
        dias--;
    }

    if (grupoViaje.getFechaHoraSalida().toLocalTime().isBefore(grupoViaje.getHoraInicioActividades())) {
        dias--;
    }

    return Math.max(dias, 0);
    }

    private List<Actividad> ordenarPorCercania(List<Actividad> actividades) {
    List<Actividad> ordenadas = new ArrayList<>();
    Set<Actividad> restantes = new HashSet<>(actividades);

    Actividad actual = restantes.iterator().next(); // punto inicial
    ordenadas.add(actual);
    restantes.remove(actual);

    while (!restantes.isEmpty()) {
        Actividad masCercana = null;
        double mejorDist = Double.MAX_VALUE;

        for (Actividad candidata : restantes) {
            double dist = distancia(actual, candidata);

            if (dist < mejorDist) {
                mejorDist = dist;
                masCercana = candidata;
            }
        }

        ordenadas.add(masCercana);
        restantes.remove(masCercana);
        actual = masCercana;
    }

    return ordenadas;
    }

    private double distancia(Actividad a, Actividad b) {
    double lat1 = Math.toRadians(a.getUbicacion().getLatitud());
    double lon1 = Math.toRadians(a.getUbicacion().getLongitud());
    double lat2 = Math.toRadians(b.getUbicacion().getLatitud());
    double lon2 = Math.toRadians(b.getUbicacion().getLongitud());

    double dLat = lat2 - lat1;
    double dLon = lon2 - lon1;

    double h = Math.sin(dLat / 2) * Math.sin(dLat / 2)
            + Math.cos(lat1) * Math.cos(lat2)
            * Math.sin(dLon / 2) * Math.sin(dLon / 2);

    double R = 6371; // radio Tierra en km

    return 2 * R * Math.asin(Math.sqrt(h));
    }

    private int calcularTraslado(Actividad a, Actividad b) {
    double dist = distancia(a, b);

    return (int) Math.round(dist * 6); 
    // ajusta este factor según ciudad (ej: 100 → minutos)
    }

    public void generarItinerarioOptimizado(
        GrupoViaje grupo,
        Itinerario itinerario,
        List<Actividad> actividades,
        int minutosPorDia
    ) {

    List<Actividad> ordenadas = ordenarPorCercania(actividades);

    LocalDate fechaActual = grupo.getFechaHoraLlegada().toLocalDate();
    LocalDate fechaFin = grupo.getFechaHoraSalida().toLocalDate();

    // 🔥 ajuste por hora de llegada
    if (grupo.getFechaHoraLlegada().toLocalTime().isAfter(grupo.getHoraInicioActividades())) {

    fechaActual = fechaActual.plusDays(1);
    }

    int i = 0;

    while (!fechaActual.isAfter(fechaFin) && i < ordenadas.size()) {

        LocalDateTime cursor = LocalDateTime.of(
                fechaActual,
                grupo.getHoraInicioActividades()
        );

        LocalDateTime almuerzoInicio = LocalDateTime.of(
                fechaActual,
                grupo.getHoraAlmuerzo()
        );

        LocalDateTime almuerzoFin = almuerzoInicio.plusMinutes(
                grupo.getDuracionAlmuerzoMin()
        );

        int tiempoUsado = 0;
        Actividad anterior = null;

        while (i < ordenadas.size()) {

            Actividad act = ordenadas.get(i);

            int traslado = 0;
            if (anterior != null) {
                traslado = calcularTraslado(anterior, act);
            }

            int duracionTotal= act.getDuracionMin() + traslado;

            // 🔥 CORTE POR TIEMPO DIARIO
            if (tiempoUsado + duracionTotal > minutosPorDia) {
                break;
            }

            // 🔥 aplicar traslado al reloj
            cursor = cursor.plusMinutes(traslado);

            LocalDateTime finActividad= cursor.plusMinutes(act.getDuracionMin());

            // 🔥 manejo de almuerzo
            if (cursor.isBefore(almuerzoInicio) && finActividad.isAfter(almuerzoInicio)) {
                cursor = almuerzoFin;
                continue;
            }

            // ✅ agregar actividad
            itemService.agregarActividadAItinerario(cursor, finActividad, itinerario.getId(), act.getId());

            tiempoUsado += duracionTotal;
            cursor= finActividad;
            anterior = act;
            i++;
        }

        fechaActual = fechaActual.plusDays(1);
    }
    }

   class Item {
    protected Actividad actividad;
    protected int utilidad;
    protected int costo;
    protected int tiempo;

    public Item(Actividad actividad, int utilidad, double costo, int tiempo) {
        this.actividad = actividad;
        this.utilidad = utilidad;
        this.costo= (int)Math.round(costo * 10);
        this.tiempo=tiempo;
    }
}
}