package com.chocoaventura.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
        List<LocalDate> dias= obtenerDiasValidos(grupoViaje);
        List<Actividad> actividadesSeleccionadas= knapsack2D(utilidades, datos, dias.size(), grupoViaje);
        
        Itinerario itinerario = new Itinerario(nombre, datos[0], grupoViaje);
        int maxMinutos= (int) Math.round(datos[1] * 60);
        generarItinerario(grupoViaje, itinerario, dias, actividadesSeleccionadas, maxMinutos);
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
        int dias,
        GrupoViaje grupoViaje
    ) {
    int n= items.size();
    // DP
    int maxCosto= (int)Math.round(datos[0] * 10);
    int maxMinutos= (int) Math.round(datos[1] * 60);
    int maxTiempo= maxMinutos* dias;
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

    private List<LocalDate> obtenerDiasValidos(GrupoViaje grupo) {
    List<LocalDate> dias = new ArrayList<>();

    LocalDate inicio = grupo.getFechaHoraLlegada().toLocalDate();
    LocalDate fin = grupo.getFechaHoraSalida().toLocalDate();

    if (grupo.getFechaHoraLlegada().toLocalTime()
            .isAfter(grupo.getHoraInicioActividades())) {
        inicio = inicio.plusDays(1);
    }

    if (grupo.getFechaHoraSalida().toLocalTime()
            .isBefore(grupo.getHoraInicioActividades())) {
        fin = fin.minusDays(1);
    }

    while (!inicio.isAfter(fin)) {
        dias.add(inicio);
        inicio = inicio.plusDays(1);
    }

    return dias;
    }

    private Map<LocalDate, List<Actividad>> distribuirActividades(
        List<Actividad> actividades,
        List<LocalDate> dias,
        int minutosPorDia
    ) {

    Map<LocalDate, List<Actividad>> asignacion = new HashMap<>();
    Map<LocalDate, Integer> tiempoUsado = new HashMap<>();

    for (LocalDate d : dias) {
        asignacion.put(d, new ArrayList<>());
        tiempoUsado.put(d, 0);
    }

    //ordenar por duración (grandes primero)
    actividades.sort((a, b) -> b.getDuracionMin() - a.getDuracionMin());

    double alpha= 1.0;   // peso tiempo
    double beta= 50.0;   // peso distancia (ajustable)

    for (Actividad act : actividades) {

        LocalDate mejorDia = null;
        double mejorScore = Double.MAX_VALUE;

        for (LocalDate d : dias) {

            int usado = tiempoUsado.get(d);
            int duracion = act.getDuracionMin();

            if (usado + duracion > minutosPorDia) continue;

            double dist = distanciaPromedio(act, asignacion.get(d));

            double score = alpha * usado + beta * dist;

            if (score < mejorScore) {
                mejorScore = score;
                mejorDia = d;
            }
        }

        if (mejorDia == null) {
            throw new RuntimeException("No se pudo asignar actividad");
        }

        asignacion.get(mejorDia).add(act);
        tiempoUsado.put(mejorDia,
                tiempoUsado.get(mejorDia) + act.getDuracionMin());
    }

    return asignacion;
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
    }

    private double distanciaPromedio(Actividad act, List<Actividad> actividadesDia) {
    if (actividadesDia.isEmpty()) return 0;

    double suma = 0;

    for (Actividad a : actividadesDia) {
        suma += distancia(act, a);
    }

    return suma / actividadesDia.size();
    }

    public void generarItinerario(
        GrupoViaje grupo,
        Itinerario itinerario,
        List<LocalDate> dias,
        List<Actividad> actividades,
        int minutosPorDia
    ) {

    Map<LocalDate, List<Actividad>> asignacion =
            distribuirActividades(actividades, dias, minutosPorDia);

    for (LocalDate dia : dias) {

        List<Actividad> actsDia = ordenarPorCercania(asignacion.get(dia));

        LocalDateTime cursor = LocalDateTime.of(
                dia,
                grupo.getHoraInicioActividades()
        );

        LocalDateTime almuerzoInicio = LocalDateTime.of(dia, grupo.getHoraAlmuerzo());
        LocalDateTime almuerzoFin = almuerzoInicio.plusMinutes(grupo.getDuracionAlmuerzoMin());

        Actividad anterior = null;

        for (Actividad act : actsDia) {

            if (anterior != null) {
                cursor = cursor.plusMinutes(calcularTraslado(anterior, act));
            }

            LocalDateTime fin = cursor.plusMinutes(act.getDuracionMin());

            if (cursor.isBefore(almuerzoInicio) && fin.isAfter(almuerzoInicio)) {
                cursor = almuerzoFin;
                fin = cursor.plusMinutes(act.getDuracionMin());
            }

            itemService.agregarActividadAItinerario(cursor, fin, itinerario.getId(), act.getId());

            cursor = fin;
            anterior = act;
        }
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