package com.chocoaventura.services;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.AsignacionTokens;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Itinerario;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.RondaSubasta;
import com.chocoaventura.entities.enums.EstadoGrupoViaje;
import com.chocoaventura.repositories.ActividadRepository;
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

    @Autowired
    private ActividadRepository actividadRepository;

    @Autowired
    private PriorizacionCategoriasService priorizacionCategoriasService;

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

    @Transactional
    public Itinerario crearItinerario(String nombre, Long grupoViajeId) {
        GrupoViaje grupoViaje = grupoViajeRepository.findById(grupoViajeId).orElseThrow(() -> new EntityNotFoundException("Grupo de viaje no encontrado con id: " + grupoViajeId));
        List<Itinerario> existentes = itinerarioRepository.findByGrupoViajeId(grupoViajeId);
        if (!existentes.isEmpty()) {
            return existentes.stream()
                    .filter(i -> i.getId() != null)
                    .max((a, b) -> a.getId().compareTo(b.getId()))
                    .orElse(existentes.get(0));
        }
        priorizacionCategoriasService.validarGrupoListoParaItinerario(grupoViaje);
        double [] datos=obtenerPresupuestoHoraPromedio(grupoViaje);
        List<Item> utilidades = construirItemsParaKnapsack(grupoViaje);
        if (utilidades.isEmpty()) {
            utilidades = actividadRepository.findAll().stream()
                    .limit(6)
                    .map(a -> new Item(a, 1, a.getCostoPorPersona(), a.getDuracionMin()))
                    .toList();
        }

        List<LocalDate> dias= obtenerDiasValidos(grupoViaje);
        List<Actividad> actividadesSeleccionadas= knapsack2D(utilidades, datos, dias.size());
        
        Itinerario itinerario= new Itinerario(nombre, datos[0], grupoViaje);
        itinerario = itinerarioRepository.save(itinerario);
        int maxMinutos= (int) Math.round(datos[1] * 60);
        generarItinerario(grupoViaje, itinerario, dias, actividadesSeleccionadas, maxMinutos);
        grupoViaje.getItinerarios().add(itinerario);
        grupoViaje.setEstado(EstadoGrupoViaje.ITINERARIO_GENERADO);
        grupoViajeRepository.save(grupoViaje);
        return itinerarioRepository.findById(itinerario.getId()).orElseThrow();
    }

    public Itinerario obtenerItinerarioActualPorGrupo(Long grupoViajeId) {
        List<Itinerario> itinerarios = itinerarioRepository.findByGrupoViajeId(grupoViajeId);
        if (itinerarios.isEmpty()) {
            throw new EntityNotFoundException("No hay itinerario creado para el grupo: " + grupoViajeId);
        }
        return itinerarios.stream()
                .filter(i -> i.getId() != null)
                .max((a, b) -> a.getId().compareTo(b.getId()))
                .orElse(itinerarios.get(0));
    }

    public double[] obtenerPresupuestoHoraPromedio(GrupoViaje grupoViaje){
        double[] respuesta = new double[2];
        respuesta[0] = 500000.0;
        respuesta[1] = 8.0;

        if (grupoViaje.getPerfiles() == null || grupoViaje.getPerfiles().isEmpty()) {
            return respuesta;
        }

        double presupuestoMenor = Double.MAX_VALUE;
        int totalUsuarios = 0;
        double horasTotales = 0;

        // presupuesto [0], horas por día actividad [1]
        for (Perfil viajero : grupoViaje.getPerfiles()) {
            if (viajero.getPresupuesto() != null && viajero.getPresupuesto() > 0) {
                presupuestoMenor = Math.min(presupuestoMenor, viajero.getPresupuesto());
            }
            if (viajero.getTiempoDiarioActividades() != null && viajero.getTiempoDiarioActividades() > 0) {
                horasTotales += viajero.getTiempoDiarioActividades();
                totalUsuarios++;
            }
        }

        if (presupuestoMenor != Double.MAX_VALUE) {
            respuesta[0] = presupuestoMenor;
        }
        if (totalUsuarios > 0) {
            respuesta[1] = horasTotales / totalUsuarios;
        }
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
            items.add(new Item(act, utilidad, act.getCostoPorPersona(), act.getDuracionMin()));
        }
        return items;
    }

    public List<Item> puntosDesdeActividadesSeleccionadas(GrupoViaje grupoViaje) {
        HashMap<Actividad, Integer> puntajes = new HashMap<>();
        for (Perfil perfil : grupoViaje.getPerfiles()) {
            if (!Boolean.TRUE.equals(perfil.getParticipaEnCoordinacion())) continue;
            for (Actividad actividad : perfil.getActividadesSeleccionadas()) {
                puntajes.merge(actividad, 1, Integer::sum);
            }
        }
        return puntajes.entrySet().stream()
                .map(e -> new Item(e.getKey(), e.getValue(), e.getKey().getCostoPorPersona(), e.getKey().getDuracionMin()))
                .toList();
    }

    public List<Item> construirItemsParaKnapsack(GrupoViaje grupoViaje) {
        Map<Long, ItemAcumulado> acumulados = new LinkedHashMap<>();

        if (grupoViaje.getPerfiles() != null) {
            for (Perfil perfil : grupoViaje.getPerfiles()) {
                if (!Boolean.TRUE.equals(perfil.getParticipaEnCoordinacion())) continue;
                if (perfil.getActividadesSeleccionadas() == null) continue;
                for (Actividad actividad : perfil.getActividadesSeleccionadas()) {
                    ItemAcumulado acumulado = obtenerAcumulado(acumulados, actividad);
                    if (acumulado != null) {
                        acumulado.puntaje += 20;
                    }
                }
            }
        }

        if (grupoViaje.getRondasSubasta() != null) {
            for (RondaSubasta ronda : grupoViaje.getRondasSubasta()) {
                if (ronda.getAsignacionesTokens() == null) continue;
                for (AsignacionTokens voto : ronda.getAsignacionesTokens()) {
                    ItemAcumulado acumulado = obtenerAcumulado(acumulados, voto.getActividad());
                    if (acumulado != null) {
                        acumulado.puntaje += voto.getTokensAsignados() == null ? 0 : voto.getTokensAsignados();
                    }
                }
            }
        }

        Map<Long, Integer> puntajesCategoria = priorizacionCategoriasService.obtenerPuntajesCategoriaParaGrupo(grupoViaje);
        for (ItemAcumulado acumulado : acumulados.values()) {
            acumulado.puntaje += puntajePorCategorias(acumulado.actividad, puntajesCategoria);
            acumulado.puntaje += puntajePorRating(acumulado.actividad);
        }

        return acumulados.values().stream()
                .map(acumulado -> new Item(
                        acumulado.actividad,
                        Math.max(1, acumulado.puntaje),
                        acumulado.actividad.getCostoPorPersona(),
                        acumulado.actividad.getDuracionMin()
                ))
                .toList();
    }

    private ItemAcumulado obtenerAcumulado(Map<Long, ItemAcumulado> acumulados, Actividad actividad) {
        if (actividad == null || actividad.getId() == null) {
            return null;
        }
        return acumulados.computeIfAbsent(actividad.getId(), id -> new ItemAcumulado(actividad));
    }

    private int puntajePorCategorias(Actividad actividad, Map<Long, Integer> puntajesCategoria) {
        if (actividad == null || actividad.getCategorias() == null || puntajesCategoria == null || puntajesCategoria.isEmpty()) {
            return 0;
        }
        int total = 0;
        for (var categoria : actividad.getCategorias()) {
            if (categoria == null || categoria.getId() == null) continue;
            total += puntajesCategoria.getOrDefault(categoria.getId(), 0);
        }
        return total;
    }

    private int puntajePorRating(Actividad actividad) {
        if (actividad == null || actividad.getCalificacionPromedio() == null) {
            return 0;
        }
        return (int) Math.round(actividad.getCalificacionPromedio() * 5);
    }

    public List<Actividad> knapsack2D(
        List<Item> items,
        double[] datos,
        int dias
    ) {
        if (items == null || items.isEmpty() || dias <= 0) {
            return new ArrayList<>();
        }

        int maxCostoPesos = (int) Math.max(0, Math.round(datos[0]));
        int maxCosto = (int) Math.ceil(maxCostoPesos / 1000.0);
        int maxTiempo = (int) Math.max(0, Math.round(datos[1] * 60 * dias));

        List<Item> candidatos = items.stream()
                .filter(item -> item != null && item.actividad != null)
                .toList();

        if (candidatos.isEmpty()) {
            return new ArrayList<>();
        }

        if (maxCosto <= 0 || maxTiempo <= 0 || ((long) maxCosto * (long) maxTiempo) > 1_500_000L) {
            return knapsackGreedy(candidatos, maxCostoPesos, maxTiempo);
        }

        Map<Long, NodoKnapsack> estados = new HashMap<>();
        estados.put(claveEstado(0, 0, maxTiempo), new NodoKnapsack(0, 0, 0, null, null));

        for (Item item : candidatos) {
            int costoItem = (int) Math.ceil(Math.max(0, item.costo) / 1000.0);
            int tiempoItem = Math.max(1, item.tiempo);
            if (costoItem > maxCosto || tiempoItem > maxTiempo) continue;

            List<NodoKnapsack> snapshot = new ArrayList<>(estados.values());
            for (NodoKnapsack estado : snapshot) {
                int nuevoCosto = estado.costo + costoItem;
                int nuevoTiempo = estado.tiempo + tiempoItem;
                if (nuevoCosto > maxCosto || nuevoTiempo > maxTiempo) continue;

                int nuevoValor = estado.valor + Math.max(1, item.utilidad);
                long nuevaClave = claveEstado(nuevoCosto, nuevoTiempo, maxTiempo);
                NodoKnapsack existente = estados.get(nuevaClave);
                if (existente == null || nuevoValor > existente.valor) {
                    estados.put(nuevaClave, new NodoKnapsack(nuevoCosto, nuevoTiempo, nuevoValor, estado, item));
                }
            }
        }

        NodoKnapsack mejor = estados.values().stream()
                .max((a, b) -> {
                    int porValor = Integer.compare(a.valor, b.valor);
                    if (porValor != 0) return porValor;
                    int porTiempo = Integer.compare(b.tiempo, a.tiempo);
                    if (porTiempo != 0) return porTiempo;
                    return Integer.compare(b.costo, a.costo);
                })
                .orElse(null);

        List<Actividad> seleccionadas = reconstruirSeleccion(mejor);
        if (seleccionadas.isEmpty()) {
            return knapsackGreedy(candidatos, maxCostoPesos, maxTiempo);
        }
        return seleccionadas;
    }

    private List<Actividad> knapsackGreedy(List<Item> items, int maxCostoPesos, int maxTiempo) {
        int costoUsado = 0;
        int tiempoUsado = 0;
        List<Actividad> seleccionadas = new ArrayList<>();
        List<Item> ordenados = new ArrayList<>(items);
        ordenados.sort((a, b) -> Double.compare(b.score(), a.score()));

        for (Item item : ordenados) {
            if (item.actividad == null) continue;
            if (maxCostoPesos > 0 && costoUsado + item.costo > maxCostoPesos) continue;
            if (maxTiempo > 0 && tiempoUsado + item.tiempo > maxTiempo) continue;
            seleccionadas.add(item.actividad);
            costoUsado += item.costo;
            tiempoUsado += item.tiempo;
        }

        if (seleccionadas.isEmpty() && !ordenados.isEmpty()) {
            seleccionadas.add(ordenados.get(0).actividad);
        }
        return seleccionadas;
    }

    private long claveEstado(int costo, int tiempo, int maxTiempo) {
        return ((long) costo * (long) (maxTiempo + 1)) + tiempo;
    }

    private List<Actividad> reconstruirSeleccion(NodoKnapsack nodo) {
        List<Actividad> seleccionadas = new ArrayList<>();
        NodoKnapsack actual = nodo;
        while (actual != null && actual.item != null) {
            seleccionadas.add(0, actual.item.actividad);
            actual = actual.anterior;
        }
        return seleccionadas;
    }

    private List<LocalDate> obtenerDiasValidos(GrupoViaje grupo) {
        List<LocalDate> dias = new ArrayList<>();

        if (grupo.getFechaHoraLlegada() == null || grupo.getFechaHoraSalida() == null) {
            dias.add(LocalDate.now());
            return dias;
        }

        LocalTime horaInicio = horaInicioActividades(grupo);
        LocalDate inicio = grupo.getFechaHoraLlegada().toLocalDate();
        LocalDate fin = grupo.getFechaHoraSalida().toLocalDate();

        if (grupo.getFechaHoraLlegada().toLocalTime().isAfter(horaInicio)) {
            inicio = inicio.plusDays(1);
        }

        if (grupo.getFechaHoraSalida().toLocalTime().isBefore(horaInicio)) {
            fin = fin.minusDays(1);
        }

        while (!inicio.isAfter(fin)) {
            dias.add(inicio);
            inicio = inicio.plusDays(1);
        }

        if (dias.isEmpty()) {
            dias.add(grupo.getFechaHoraLlegada().toLocalDate());
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
    actividades.sort((a, b) -> Integer.compare(duracionActividad(b), duracionActividad(a)));

    double alpha= 1.0;   // peso tiempo
    double beta= 50.0;   // peso distancia (ajustable)

    for (Actividad act : actividades) {

        LocalDate mejorDia = null;
        double mejorScore = Double.MAX_VALUE;

        for (LocalDate d : dias) {

            int usado = tiempoUsado.get(d);
            int duracion = duracionActividad(act);

            if (usado + duracion > minutosPorDia) continue;

            double dist = distanciaPromedio(act, asignacion.get(d));

            double score = alpha * usado + beta * dist;

            if (score < mejorScore) {
                mejorScore = score;
                mejorDia = d;
            }
        }

        if (mejorDia == null) {
            continue;
        }

        asignacion.get(mejorDia).add(act);
        tiempoUsado.put(mejorDia,
                tiempoUsado.get(mejorDia) + duracionActividad(act));
    }

    return asignacion;
    }

    private List<Actividad> ordenarPorCercania(List<Actividad> actividades) {
    List<Actividad> ordenadas = new ArrayList<>();
    if (actividades == null || actividades.isEmpty()) return ordenadas;
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
    if (a == null || b == null || a.getUbicacion() == null || b.getUbicacion() == null ||
            a.getUbicacion().getLatitud() == null || a.getUbicacion().getLongitud() == null ||
            b.getUbicacion().getLatitud() == null || b.getUbicacion().getLongitud() == null) {
        return 0.0;
    }
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

    if (actividades == null || actividades.isEmpty() || dias == null || dias.isEmpty()) return;

    Map<LocalDate, List<Actividad>> asignacion =
            distribuirActividades(actividades, dias, minutosPorDia);

    for (LocalDate dia : dias) {

        List<Actividad> actsDia = ordenarPorCercania(asignacion.get(dia));

        LocalDateTime cursor = LocalDateTime.of(
                dia,
                horaInicioActividades(grupo)
        );

        LocalDateTime almuerzoInicio = LocalDateTime.of(dia, horaAlmuerzo(grupo));
        LocalDateTime almuerzoFin = almuerzoInicio.plusMinutes(duracionAlmuerzo(grupo));

        Actividad anterior = null;

        for (Actividad act : actsDia) {

            if (anterior != null) {
                cursor = cursor.plusMinutes(calcularTraslado(anterior, act));
            }

            int duracion = duracionActividad(act);
            LocalDateTime fin = cursor.plusMinutes(duracion);

            if (cursor.isBefore(almuerzoInicio) && fin.isAfter(almuerzoInicio)) {
                cursor = almuerzoFin;
                fin = cursor.plusMinutes(duracion);
            }

            itemService.agregarActividadAItinerario(cursor, fin, itinerario.getId(), act.getId());

            cursor = fin;
            anterior = act;
        }
    }
    }

    private LocalTime horaInicioActividades(GrupoViaje grupo) {
        return grupo.getHoraInicioActividades() == null ? LocalTime.of(9, 0) : grupo.getHoraInicioActividades();
    }

    private LocalTime horaAlmuerzo(GrupoViaje grupo) {
        return grupo.getHoraAlmuerzo() == null ? LocalTime.of(13, 0) : grupo.getHoraAlmuerzo();
    }

    private int duracionAlmuerzo(GrupoViaje grupo) {
        return grupo.getDuracionAlmuerzoMin() == null || grupo.getDuracionAlmuerzoMin() <= 0 ? 60 : grupo.getDuracionAlmuerzoMin();
    }

    private int duracionActividad(Actividad actividad) {
        if (actividad == null || actividad.getDuracionMin() == null || actividad.getDuracionMin() <= 0) {
            return 60;
        }
        return actividad.getDuracionMin();
    }

   class NodoKnapsack {
    protected int costo;
    protected int tiempo;
    protected int valor;
    protected NodoKnapsack anterior;
    protected Item item;

    public NodoKnapsack(int costo, int tiempo, int valor, NodoKnapsack anterior, Item item) {
        this.costo = costo;
        this.tiempo = tiempo;
        this.valor = valor;
        this.anterior = anterior;
        this.item = item;
    }
}

   class ItemAcumulado {
    protected Actividad actividad;
    protected int puntaje;

    public ItemAcumulado(Actividad actividad) {
        this.actividad = actividad;
        this.puntaje = 0;
    }
}

   class Item {
    protected Actividad actividad;
    protected int utilidad;
    protected int costo;
    protected int tiempo;

    public Item(Actividad actividad, int utilidad, Double costo, Integer tiempo) {
        this.actividad = actividad;
        this.utilidad = utilidad;
        this.costo = costo == null || costo < 0 ? 0 : (int) Math.round(costo);
        this.tiempo = tiempo == null || tiempo <= 0 ? 60 : tiempo;
    }

    double score() {
        double divisor = Math.max(1, costo / 1000.0) + Math.max(1, tiempo / 60.0);
        return utilidad / divisor;
    }
}
}