package com.chocoaventura.config;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Categoria;
import com.chocoaventura.entities.Ciudad;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Imagen;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.entities.Ubicacion;
import com.chocoaventura.entities.Usuario;
import com.chocoaventura.entities.enums.EstadoGrupoViaje;
import com.chocoaventura.repositories.ActividadRepository;
import com.chocoaventura.repositories.CategoriaRepository;
import com.chocoaventura.repositories.CiudadRepository;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.PerfilRepository;
import com.chocoaventura.repositories.UbicacionRepository;
import com.chocoaventura.repositories.UsuarioRepository;

@Configuration
public class SeedDataConfig {

    @Bean
    CommandLineRunner seedData(
            UsuarioRepository usuarioRepository,
            CategoriaRepository categoriaRepository,
            CiudadRepository ciudadRepository,
            UbicacionRepository ubicacionRepository,
            ActividadRepository actividadRepository,
            GrupoViajeRepository grupoViajeRepository,
            PerfilRepository perfilRepository
    ) {
        return args -> {
            if (usuarioRepository.count() > 0) {
                return;
            }
            Map<String, Categoria> categorias = crearCategorias(categoriaRepository);
            Map<String, Ciudad> ciudades = crearCiudades(ciudadRepository);
            Map<String, Ubicacion> ubicaciones = crearUbicaciones(ubicacionRepository);
            List<Actividad> actividades = crearActividadesDesdeAssets(actividadRepository, ciudades, ubicaciones, categorias);

            Usuario laura = usuarioRepository.save(new Usuario("Laura", "laura@choco.com", "123456"));
            Usuario cata = usuarioRepository.save(new Usuario("Cata", "cata@choco.com", "123456"));
            Usuario michelle = usuarioRepository.save(new Usuario("Michelle", "michelle@choco.com", "123456"));
            Usuario fer = usuarioRepository.save(new Usuario("Fernando", "fernando@choco.com", "123456"));
            Usuario sara = usuarioRepository.save(new Usuario("Sara", "sara@choco.com", "123456"));
            Usuario juan = usuarioRepository.save(new Usuario("Juan", "juan@choco.com", "123456"));

            Ciudad medellin = ciudades.get("medellin");
            GrupoViaje grupo = new GrupoViaje(
                    "Viaje ChocoAventura Medellín",
                    "Viaje grupal para probar exploración grupal con actividades servidas desde el backend",
                    LocalTime.of(8, 0),
                    LocalTime.of(13, 0),
                    60,
                    LocalDateTime.of(2026, 4, 10, 9, 0),
                    LocalDateTime.of(2026, 4, 20, 18, 0),
                    medellin,
                    laura
            );
            grupo.setEstadia(ubicaciones.get("medellin"));
            grupo.setEstado(EstadoGrupoViaje.CONFIRMACION_GRUPAL_PENDIENTE);
            grupo = grupoViajeRepository.save(grupo);

            List<Actividad> medellinActs = actividades.stream()
                    .filter(a -> a.getCiudad() != null && "Medellín".equalsIgnoreCase(a.getCiudad().getNombre()))
                    .collect(Collectors.toList());

            Perfil perfilLaura = crearPerfil(laura, grupo, 500000.0, 0, 360, true, true,
                    Set.of(categorias.get("Cultura e historia"), categorias.get("Gastronomía")));
            Perfil perfilCata = crearPerfil(cata, grupo, 400000.0, 0, 300, true, true,
                    Set.of(categorias.get("Naturaleza y aventura"), categorias.get("Experiencias auténticas")));
            Perfil perfilMichelle = crearPerfil(michelle, grupo, 450000.0, 0, 320, true, true,
                    Set.of(categorias.get("Relax"), categorias.get("Cultura e historia")));
            Perfil perfilFer = crearPerfil(fer, grupo, 380000.0, 0, 280, true, true,
                    Set.of(categorias.get("Gastronomía"), categorias.get("Vida nocturna")));
            Perfil perfilSara = crearPerfil(sara, grupo, 350000.0, 0, 250, true, true,
                    Set.of(categorias.get("Experiencia local")));
            Perfil perfilJuan = crearPerfil(juan, grupo, 420000.0, 0, 300, true, false,
                    Set.of(categorias.get("Naturaleza y aventura")));

            seleccionarPorIndice(perfilLaura, medellinActs, 0, 3, 6, 10);
            seleccionarPorIndice(perfilCata, medellinActs, 1, 4, 7, 12);
            seleccionarPorIndice(perfilMichelle, medellinActs, 2, 5, 8, 14);
            seleccionarPorIndice(perfilFer, medellinActs, 9, 11, 13, 15);
            seleccionarPorIndice(perfilSara, medellinActs, 16, 17, 18, 19);

            perfilRepository.saveAll(List.of(
                    perfilLaura, perfilCata, perfilMichelle, perfilFer, perfilSara, perfilJuan
            ));
        };
    }

    private Map<String, Categoria> crearCategorias(CategoriaRepository categoriaRepository) {
        List<Categoria> base = List.of(
                new Categoria("Experiencia local", "Planes locales y flexibles"),
                new Categoria("Mar y playa", "Actividades de agua, playa y navegación"),
                new Categoria("Cultura e historia", "Museos, recorridos históricos y patrimonio"),
                new Categoria("Gastronomía", "Comida típica, cafés, mercados y degustaciones"),
                new Categoria("Naturaleza y aventura", "Senderismo, miradores y actividades al aire libre"),
                new Categoria("Vida nocturna", "Salsa, música en vivo, bares y planes nocturnos"),
                new Categoria("Relax", "Planes suaves, picnic y descanso"),
                new Categoria("Experiencias auténticas", "Tours locales, comunidad y experiencias de territorio"),
                new Categoria("Amazonía", "Naturaleza, río, selva y cultura amazónica")
        );
        return categoriaRepository.saveAll(base).stream()
                .collect(Collectors.toMap(Categoria::getNombre, c -> c, (a, b) -> a, HashMap::new));
    }

    private Map<String, Ciudad> crearCiudades(CiudadRepository ciudadRepository) {
        Map<String, Ciudad> ciudades = new HashMap<>();
        ciudades.put("bogota", ciudadRepository.save(new Ciudad("Bogotá", "Colombia")));
        ciudades.put("medellin", ciudadRepository.save(new Ciudad("Medellín", "Colombia")));
        ciudades.put("cartagena", ciudadRepository.save(new Ciudad("Cartagena", "Colombia")));
        ciudades.put("amazonas", ciudadRepository.save(new Ciudad("Amazonas", "Colombia")));
        ciudades.put("cali", ciudadRepository.save(new Ciudad("Cali", "Colombia")));
        return ciudades;
    }

    private Map<String, Ubicacion> crearUbicaciones(UbicacionRepository ubicacionRepository) {
        Map<String, Ubicacion> ubicaciones = new HashMap<>();
        ubicaciones.put("bogota", ubicacionRepository.save(new Ubicacion("Zona típica de Bogotá", "Bogotá, Colombia", 4.7110, -74.0721)));
        ubicaciones.put("medellin", ubicacionRepository.save(new Ubicacion("Zona típica de Medellín", "Medellín, Colombia", 6.2442, -75.5812)));
        ubicaciones.put("cartagena", ubicacionRepository.save(new Ubicacion("Zona típica de Cartagena", "Cartagena, Colombia", 10.3910, -75.4794)));
        ubicaciones.put("amazonas", ubicacionRepository.save(new Ubicacion("Zona típica de Amazonas", "Amazonas, Colombia", -4.2153, -69.9406)));
        ubicaciones.put("cali", ubicacionRepository.save(new Ubicacion("Zona típica de Cali", "Cali, Colombia", 3.4516, -76.5320)));
        return ubicaciones;
    }

    private List<Actividad> crearActividadesDesdeAssets(
            ActividadRepository actividadRepository,
            Map<String, Ciudad> ciudades,
            Map<String, Ubicacion> ubicaciones,
            Map<String, Categoria> categorias
    ) {
        List<ActividadSeed> seeds = List.of(
                new ActividadSeed("amazonas", "Aprendizaje De Plantas Medicinales", "Plan en Amazonas para disfrutar Aprendizaje De Plantas Medicinales con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 122000.0, 240, 4.0, "Amazonía", "/assets/actividades/amazonas/Aprendizaje%20De%20Plantas%20Medicinales.jpg"),
                new ActividadSeed("amazonas", "Avistamiento De Aves", "Plan en Amazonas para disfrutar Avistamiento De Aves con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 122000.0, 240, 4.0, "Amazonía", "/assets/actividades/amazonas/Avistamiento%20De%20Aves.jpg"),
                new ActividadSeed("amazonas", "Avistamiento De Delfines Rosados", "Plan en Amazonas para disfrutar Avistamiento De Delfines Rosados con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 125000.0, 120, 4.0, "Amazonía", "/assets/actividades/amazonas/Avistamiento%20De%20Delfines%20Rosados.jpg"),
                new ActividadSeed("amazonas", "Caminata Guiada Por La Selva", "Plan en Amazonas para disfrutar Caminata Guiada Por La Selva con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 132000.0, 240, 4.3, "Amazonía", "/assets/actividades/amazonas/Caminata%20Guiada%20Por%20La%20Selva.jpg"),
                new ActividadSeed("amazonas", "Canopy En La Selva", "Plan en Amazonas para disfrutar Canopy En La Selva con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 132000.0, 240, 4.3, "Amazonía", "/assets/actividades/amazonas/Canopy%20En%20La%20Selva.jpg"),
                new ActividadSeed("amazonas", "Comida Típica Amazónica", "Plan en Amazonas para disfrutar Comida Típica Amazónica con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 65000.0, 120, 4.6, "Gastronomía", "/assets/actividades/amazonas/Comida%20Ti%CC%81pica%20Amazo%CC%81nica.jpg"),
                new ActividadSeed("amazonas", "Cruce De La Triple Frontera", "Plan en Amazonas para disfrutar Cruce De La Triple Frontera con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 132000.0, 120, 4.3, "Amazonía", "/assets/actividades/amazonas/Cruce%20De%20La%20Triple%20Frontera.jpg"),
                new ActividadSeed("amazonas", "Dormir En Reserva Natural", "Plan en Amazonas para disfrutar Dormir En Reserva Natural con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 135000.0, 120, 4.3, "Amazonía", "/assets/actividades/amazonas/Dormir%20En%20Reserva%20Natural.jpg"),
                new ActividadSeed("amazonas", "Escuchar Sonidos De La Selva", "Plan en Amazonas para disfrutar Escuchar Sonidos De La Selva con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 138000.0, 240, 4.5, "Amazonía", "/assets/actividades/amazonas/Escuchar%20Sonidos%20De%20La%20Selva.jpg"),
                new ActividadSeed("amazonas", "Fotografía De Naturaleza", "Plan en Amazonas para disfrutar Fotografía De Naturaleza con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 125000.0, 240, 4.2, "Amazonía", "/assets/actividades/amazonas/Fotografi%CC%81a%20De%20Naturaleza.jpg"),
                new ActividadSeed("amazonas", "Inmersión Cultural Con Guías Locales", "Plan en Amazonas para disfrutar Inmersión Cultural Con Guías Locales con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 62000.0, 120, 4.5, "Cultura e historia", "/assets/actividades/amazonas/Inmersio%CC%81n%20Cultural%20Con%20Gui%CC%81as%20Locales.jpg"),
                new ActividadSeed("amazonas", "Kayak En Aguas Tranquilas", "Plan en Amazonas para disfrutar Kayak En Aguas Tranquilas con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 140000.0, 120, 4.4, "Amazonía", "/assets/actividades/amazonas/Kayak%20En%20Aguas%20Tranquilas.jpg"),
                new ActividadSeed("amazonas", "Mercado Local Amazónico", "Plan en Amazonas para disfrutar Mercado Local Amazónico con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 78000.0, 120, 4.2, "Gastronomía", "/assets/actividades/amazonas/Mercado%20Local%20Amazo%CC%81nico.jpg"),
                new ActividadSeed("amazonas", "Navegación Por El Río Amazonas", "Plan en Amazonas para disfrutar Navegación Por El Río Amazonas con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 128000.0, 120, 4.2, "Amazonía", "/assets/actividades/amazonas/Navegacio%CC%81n%20Por%20El%20Ri%CC%81o%20Amazonas.jpg"),
                new ActividadSeed("amazonas", "Observación De Monos", "Plan en Amazonas para disfrutar Observación De Monos con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 142000.0, 240, 4.6, "Amazonía", "/assets/actividades/amazonas/Observacio%CC%81n%20De%20Monos.jpg"),
                new ActividadSeed("amazonas", "Paseo En Canoa", "Plan en Amazonas para disfrutar Paseo En Canoa con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 128000.0, 120, 4.2, "Amazonía", "/assets/actividades/amazonas/Paseo%20En%20Canoa.jpg"),
                new ActividadSeed("amazonas", "Pesca Artesanal Con Guía Local", "Plan en Amazonas para disfrutar Pesca Artesanal Con Guía Local con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 120000.0, 120, 4.0, "Amazonía", "/assets/actividades/amazonas/Pesca%20Artesanal%20Con%20Gui%CC%81a%20Local.jpg"),
                new ActividadSeed("amazonas", "Puentes Colgantes En La Selva", "Plan en Amazonas para disfrutar Puentes Colgantes En La Selva con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 122000.0, 240, 4.1, "Amazonía", "/assets/actividades/amazonas/Puentes%20Colgantes%20En%20La%20Selva.jpg"),
                new ActividadSeed("amazonas", "Recorrido Nocturno De Fauna", "Plan en Amazonas para disfrutar Recorrido Nocturno De Fauna con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 122000.0, 240, 4.6, "Amazonía", "/assets/actividades/amazonas/Recorrido%20Nocturno%20De%20Fauna.jpg"),
                new ActividadSeed("amazonas", "Visita A Comunidad Indígena", "Plan en Amazonas para disfrutar Visita A Comunidad Indígena con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 45000.0, 120, 4.6, "Experiencias auténticas", "/assets/actividades/amazonas/Visita%20A%20Comunidad%20Indi%CC%81gena.jpg"),
                new ActividadSeed("bogota", "Atardecer Desde Mirador", "Plan en Bogotá para disfrutar Atardecer Desde Mirador con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 50000.0, 120, 4.6, "Experiencia local", "/assets/actividades/bogota/Atardecer%20Desde%20Mirador.jpg"),
                new ActividadSeed("bogota", "Caminata Cultural Por La Candelaria", "Plan en Bogotá para disfrutar Caminata Cultural Por La Candelaria con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 78000.0, 240, 4.6, "Naturaleza y aventura", "/assets/actividades/bogota/Caminata%20Cultural%20Por%20La%20Candelaria.jpg"),
                new ActividadSeed("bogota", "Cata De Café Colombiano", "Plan en Bogotá para disfrutar Cata De Café Colombiano con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 85000.0, 120, 4.5, "Gastronomía", "/assets/actividades/bogota/Cata%20De%20Cafe%CC%81%20Colombiano.jpg"),
                new ActividadSeed("bogota", "Chocolate Santafereño Con Queso", "Plan en Bogotá para disfrutar Chocolate Santafereño Con Queso con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 65000.0, 120, 4.0, "Gastronomía", "/assets/actividades/bogota/Chocolate%20Santaferen%CC%83o%20Con%20Queso.jpg"),
                new ActividadSeed("bogota", "Ciclovía En Bogotá", "Plan en Bogotá para disfrutar Ciclovía En Bogotá con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 52000.0, 120, 4.1, "Experiencia local", "/assets/actividades/bogota/Ciclovi%CC%81a%20En%20Bogota%CC%81.jpg"),
                new ActividadSeed("bogota", "Clase De Baile Latino", "Plan en Bogotá para disfrutar Clase De Baile Latino con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 78000.0, 180, 4.1, "Vida nocturna", "/assets/actividades/bogota/Clase%20De%20Baile%20Latino.jpg"),
                new ActividadSeed("bogota", "Clase De Cocina Colombiana", "Plan en Bogotá para disfrutar Clase De Cocina Colombiana con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 68000.0, 120, 4.0, "Gastronomía", "/assets/actividades/bogota/Clase%20De%20Cocina%20Colombiana.jpg"),
                new ActividadSeed("bogota", "Escapada A Laguna Cercana", "Plan en Bogotá para disfrutar Escapada A Laguna Cercana con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 52000.0, 120, 4.2, "Experiencia local", "/assets/actividades/bogota/Escapada%20A%20Laguna%20Cercana.jpg"),
                new ActividadSeed("bogota", "Experiencia De Café De Especialidad", "Plan en Bogotá para disfrutar Experiencia De Café De Especialidad con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 75000.0, 120, 4.2, "Gastronomía", "/assets/actividades/bogota/Experiencia%20De%20Cafe%CC%81%20De%20Especialidad.jpg"),
                new ActividadSeed("bogota", "La Candelaria", "Plan en Bogotá para disfrutar La Candelaria con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 52000.0, 120, 4.2, "Cultura e historia", "/assets/actividades/bogota/La%20Candelaria.jpg"),
                new ActividadSeed("bogota", "Monserrate", "Plan en Bogotá para disfrutar Monserrate con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 62000.0, 120, 4.5, "Experiencia local", "/assets/actividades/bogota/Monserrate.jpg"),
                new ActividadSeed("bogota", "Museo Botero", "Plan en Bogotá para disfrutar Museo Botero con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 58000.0, 120, 4.3, "Cultura e historia", "/assets/actividades/bogota/Museo%20Botero.jpg"),
                new ActividadSeed("bogota", "Parque Simon Bolivar", "Plan en Bogotá para disfrutar Parque Simon Bolivar con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 62000.0, 120, 4.4, "Experiencia local", "/assets/actividades/bogota/Parque%20Simon%20Bolivar.jpg"),
                new ActividadSeed("bogota", "Picnic En Parque Urbano", "Plan en Bogotá para disfrutar Picnic En Parque Urbano con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 68000.0, 120, 4.5, "Relax", "/assets/actividades/bogota/Picnic%20En%20Parque%20Urbano.jpg"),
                new ActividadSeed("bogota", "Recorrido De Arte Urbano", "Plan en Bogotá para disfrutar Recorrido De Arte Urbano con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 58000.0, 120, 4.3, "Experiencias auténticas", "/assets/actividades/bogota/Recorrido%20De%20Arte%20Urbano.jpg"),
                new ActividadSeed("bogota", "Recorrido Histórico Guiado", "Plan en Bogotá para disfrutar Recorrido Histórico Guiado con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 68000.0, 120, 4.6, "Experiencias auténticas", "/assets/actividades/bogota/Recorrido%20Histo%CC%81rico%20Guiado.jpg"),
                new ActividadSeed("bogota", "Ruta Fotográfica Por El Centro Histórico", "Plan en Bogotá para disfrutar Ruta Fotográfica Por El Centro Histórico con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 45000.0, 120, 4.6, "Cultura e historia", "/assets/actividades/bogota/Ruta%20Fotogra%CC%81fica%20Por%20El%20Centro%20Histo%CC%81rico.jpg"),
                new ActividadSeed("bogota", "Senderismo En Cerros Orientales", "Plan en Bogotá para disfrutar Senderismo En Cerros Orientales con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 98000.0, 240, 4.5, "Naturaleza y aventura", "/assets/actividades/bogota/Senderismo%20En%20Cerros%20Orientales.jpg"),
                new ActividadSeed("bogota", "Teatro En Vivo", "Plan en Bogotá para disfrutar Teatro En Vivo con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 58000.0, 120, 4.2, "Cultura e historia", "/assets/actividades/bogota/Teatro%20En%20Vivo.jpg"),
                new ActividadSeed("bogota", "Tour De Comida Callejera", "Plan en Bogotá para disfrutar Tour De Comida Callejera con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 72000.0, 120, 4.1, "Gastronomía", "/assets/actividades/bogota/Tour%20De%20Comida%20Callejera.jpg"),
                new ActividadSeed("bogota", "Tour De Museos", "Plan en Bogotá para disfrutar Tour De Museos con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 48000.0, 120, 4.6, "Cultura e historia", "/assets/actividades/bogota/Tour%20De%20Museos.jpg"),
                new ActividadSeed("bogota", "Tour Gastronómico Por La Candelaria", "Plan en Bogotá para disfrutar Tour Gastronómico Por La Candelaria con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 75000.0, 120, 4.2, "Gastronomía", "/assets/actividades/bogota/Tour%20Gastrono%CC%81mico%20Por%20La%20Candelaria.jpg"),
                new ActividadSeed("bogota", "Tour Nocturno De Leyendas", "Plan en Bogotá para disfrutar Tour Nocturno De Leyendas con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 58000.0, 120, 4.3, "Experiencias auténticas", "/assets/actividades/bogota/Tour%20Nocturno%20De%20Leyendas.jpg"),
                new ActividadSeed("bogota", "Visita A Mercado Local", "Plan en Bogotá para disfrutar Visita A Mercado Local con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 68000.0, 120, 4.1, "Gastronomía", "/assets/actividades/bogota/Visita%20A%20Mercado%20Local.jpg"),
                new ActividadSeed("cali", "Atardecer Desde Cristo Rey", "Plan en Cali para disfrutar Atardecer Desde Cristo Rey con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 50000.0, 120, 4.6, "Experiencia local", "/assets/actividades/cali/Atardecer%20Desde%20Cristo%20Rey.jpg"),
                new ActividadSeed("cali", "Bailar Salsa En Discoteca Local", "Plan en Cali para disfrutar Bailar Salsa En Discoteca Local con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 75000.0, 180, 4.1, "Vida nocturna", "/assets/actividades/cali/Bailar%20Salsa%20En%20Discoteca%20Local.jpg"),
                new ActividadSeed("cali", "Baño En Río Pance", "Plan en Cali para disfrutar Baño En Río Pance con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 108000.0, 120, 4.4, "Mar y playa", "/assets/actividades/cali/Ban%CC%83o%20En%20Ri%CC%81o%20Pance.jpg"),
                new ActividadSeed("cali", "Café De Especialidad", "Plan en Cali para disfrutar Café De Especialidad con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 85000.0, 120, 4.6, "Gastronomía", "/assets/actividades/cali/Cafe%CC%81%20De%20Especialidad.jpg"),
                new ActividadSeed("cali", "Caminata Cultural Por San Antonio", "Plan en Cali para disfrutar Caminata Cultural Por San Antonio con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 80000.0, 240, 4.6, "Naturaleza y aventura", "/assets/actividades/cali/Caminata%20Cultural%20Por%20San%20Antonio.jpg"),
                new ActividadSeed("cali", "Clase De Baile Urbano", "Plan en Cali para disfrutar Clase De Baile Urbano con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 80000.0, 180, 4.3, "Vida nocturna", "/assets/actividades/cali/Clase%20De%20Baile%20Urbano.jpg"),
                new ActividadSeed("cali", "Clase De Cocina Valluna", "Plan en Cali para disfrutar Clase De Cocina Valluna con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 68000.0, 120, 4.6, "Gastronomía", "/assets/actividades/cali/Clase%20De%20Cocina%20Valluna.jpg"),
                new ActividadSeed("cali", "Clase De Salsa Caleña", "Plan en Cali para disfrutar Clase De Salsa Caleña con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 82000.0, 180, 4.3, "Vida nocturna", "/assets/actividades/cali/Clase%20De%20Salsa%20Calen%CC%83a.jpg"),
                new ActividadSeed("cali", "Comida Típica En Galería Local", "Plan en Cali para disfrutar Comida Típica En Galería Local con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 68000.0, 120, 4.6, "Gastronomía", "/assets/actividades/cali/Comida%20Ti%CC%81pica%20En%20Galeri%CC%81a%20Local.jpg"),
                new ActividadSeed("cali", "Escapada Al Kilómetro 18", "Plan en Cali para disfrutar Escapada Al Kilómetro 18 con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 65000.0, 120, 4.4, "Experiencia local", "/assets/actividades/cali/Escapada%20Al%20Kilo%CC%81metro%2018.jpg"),
                new ActividadSeed("cali", "Mercado Local Caleño", "Plan en Cali para disfrutar Mercado Local Caleño con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 75000.0, 120, 4.3, "Gastronomía", "/assets/actividades/cali/Mercado%20Local%20Calen%CC%83o.jpg"),
                new ActividadSeed("cali", "Noche De Música En Vivo", "Plan en Cali para disfrutar Noche De Música En Vivo con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 88000.0, 180, 4.3, "Vida nocturna", "/assets/actividades/cali/Noche%20De%20Mu%CC%81sica%20En%20Vivo.jpg"),
                new ActividadSeed("cali", "Picnic En Zona Verde", "Plan en Cali para disfrutar Picnic En Zona Verde con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 50000.0, 120, 4.0, "Relax", "/assets/actividades/cali/Picnic%20En%20Zona%20Verde.jpg"),
                new ActividadSeed("cali", "Probar Lulada Y Cholado", "Plan en Cali para disfrutar Probar Lulada Y Cholado con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 65000.0, 120, 4.0, "Gastronomía", "/assets/actividades/cali/Probar%20Lulada%20Y%20Cholado.jpg"),
                new ActividadSeed("cali", "Recorrido De Murales", "Plan en Cali para disfrutar Recorrido De Murales con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 62000.0, 120, 4.4, "Experiencias auténticas", "/assets/actividades/cali/Recorrido%20De%20Murales.jpg"),
                new ActividadSeed("cali", "Ruta De Miradores", "Plan en Cali para disfrutar Ruta De Miradores con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 58000.0, 120, 4.2, "Experiencia local", "/assets/actividades/cali/Ruta%20De%20Miradores.jpg"),
                new ActividadSeed("cali", "Ruta Gastronómica Valluna", "Plan en Cali para disfrutar Ruta Gastronómica Valluna con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 88000.0, 120, 4.5, "Gastronomía", "/assets/actividades/cali/Ruta%20Gastrono%CC%81mica%20Valluna.jpg"),
                new ActividadSeed("cali", "Senderismo En Los Farallones", "Plan en Cali para disfrutar Senderismo En Los Farallones con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 80000.0, 240, 4.1, "Naturaleza y aventura", "/assets/actividades/cali/Senderismo%20En%20Los%20Farallones.jpg"),
                new ActividadSeed("cali", "Sesión Fotográfica Urbana", "Plan en Cali para disfrutar Sesión Fotográfica Urbana con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 52000.0, 120, 4.1, "Experiencia local", "/assets/actividades/cali/Sesio%CC%81n%20Fotogra%CC%81fica%20Urbana.jpg"),
                new ActividadSeed("cali", "Show De Salsa En Vivo", "Plan en Cali para disfrutar Show De Salsa En Vivo con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 70000.0, 180, 4.6, "Vida nocturna", "/assets/actividades/cali/Show%20De%20Salsa%20En%20Vivo.jpg"),
                new ActividadSeed("cartagena", "Atardecer En La Muralla", "Plan en Cartagena para disfrutar Atardecer En La Muralla con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 48000.0, 120, 4.0, "Cultura e historia", "/assets/actividades/cartagena/Atardecer%20En%20La%20Muralla.jpg"),
                new ActividadSeed("cartagena", "Baile De Champeta", "Plan en Cartagena para disfrutar Baile De Champeta con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 80000.0, 180, 4.2, "Vida nocturna", "/assets/actividades/cartagena/Baile%20De%20Champeta.jpg"),
                new ActividadSeed("cartagena", "Cena Frente Al Mar", "Plan en Cartagena para disfrutar Cena Frente Al Mar con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 78000.0, 120, 4.2, "Gastronomía", "/assets/actividades/cartagena/Cena%20Frente%20Al%20Mar.jpg"),
                new ActividadSeed("cartagena", "Clase De Baile Caribeño", "Plan en Cartagena para disfrutar Clase De Baile Caribeño con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 75000.0, 180, 4.0, "Vida nocturna", "/assets/actividades/cartagena/Clase%20De%20Baile%20Cariben%CC%83o.jpg"),
                new ActividadSeed("cartagena", "Clase De Cocina Cartagenera", "Plan en Cartagena para disfrutar Clase De Cocina Cartagenera con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 65000.0, 120, 4.6, "Gastronomía", "/assets/actividades/cartagena/Clase%20De%20Cocina%20Cartagenera.jpg"),
                new ActividadSeed("cartagena", "Cócteles En Terraza Histórica", "Plan en Cartagena para disfrutar Cócteles En Terraza Histórica con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 55000.0, 120, 4.3, "Experiencia local", "/assets/actividades/cartagena/Co%CC%81cteles%20En%20Terraza%20Histo%CC%81rica.jpg"),
                new ActividadSeed("cartagena", "Degustación De Dulces Típicos", "Plan en Cartagena para disfrutar Degustación De Dulces Típicos con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 80000.0, 120, 4.4, "Gastronomía", "/assets/actividades/cartagena/Degustacio%CC%81n%20De%20Dulces%20Ti%CC%81picos.jpg"),
                new ActividadSeed("cartagena", "Día De Playa En Barú", "Plan en Cartagena para disfrutar Día De Playa En Barú con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 102000.0, 120, 4.2, "Mar y playa", "/assets/actividades/cartagena/Di%CC%81a%20De%20Playa%20En%20Baru%CC%81.jpg"),
                new ActividadSeed("cartagena", "Island Hopping", "Plan en Cartagena para disfrutar Island Hopping con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 105000.0, 120, 4.2, "Mar y playa", "/assets/actividades/cartagena/Island%20Hopping.jpg"),
                new ActividadSeed("cartagena", "Kayak En Manglares", "Plan en Cartagena para disfrutar Kayak En Manglares con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 100000.0, 120, 4.1, "Mar y playa", "/assets/actividades/cartagena/Kayak%20En%20Manglares.jpg"),
                new ActividadSeed("cartagena", "Paddleboard En El Mar", "Plan en Cartagena para disfrutar Paddleboard En El Mar con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 95000.0, 120, 4.0, "Mar y playa", "/assets/actividades/cartagena/Paddleboard%20En%20El%20Mar.jpg"),
                new ActividadSeed("cartagena", "Paseo En Barco Por La Bahía", "Plan en Cartagena para disfrutar Paseo En Barco Por La Bahía con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 102000.0, 120, 4.3, "Mar y playa", "/assets/actividades/cartagena/Paseo%20En%20Barco%20Por%20La%20Bahi%CC%81a.jpg"),
                new ActividadSeed("cartagena", "Paseo En Bicicleta Por Barrios Históricos", "Plan en Cartagena para disfrutar Paseo En Bicicleta Por Barrios Históricos con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 108000.0, 120, 4.3, "Mar y playa", "/assets/actividades/cartagena/Paseo%20En%20Bicicleta%20Por%20Barrios%20Histo%CC%81ricos.jpg"),
                new ActividadSeed("cartagena", "Paseo En Lancha", "Plan en Cartagena para disfrutar Paseo En Lancha con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 92000.0, 120, 4.1, "Mar y playa", "/assets/actividades/cartagena/Paseo%20En%20Lancha.jpg"),
                new ActividadSeed("cartagena", "Recorrido Por Plazas Coloniales", "Plan en Cartagena para disfrutar Recorrido Por Plazas Coloniales con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 48000.0, 120, 4.6, "Cultura e historia", "/assets/actividades/cartagena/Recorrido%20Por%20Plazas%20Coloniales.jpg"),
                new ActividadSeed("cartagena", "Ruta Fotográfica Colonial", "Plan en Cartagena para disfrutar Ruta Fotográfica Colonial con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 58000.0, 120, 4.3, "Cultura e historia", "/assets/actividades/cartagena/Ruta%20Fotogra%CC%81fica%20Colonial.jpg"),
                new ActividadSeed("cartagena", "Snorkel En Islas", "Plan en Cartagena para disfrutar Snorkel En Islas con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 105000.0, 120, 4.3, "Mar y playa", "/assets/actividades/cartagena/Snorkel%20En%20Islas.jpg"),
                new ActividadSeed("cartagena", "Tour Cultural Por Getsemaní", "Plan en Cartagena para disfrutar Tour Cultural Por Getsemaní con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 50000.0, 120, 4.0, "Cultura e historia", "/assets/actividades/cartagena/Tour%20Cultural%20Por%20Getsemani%CC%81.jpg"),
                new ActividadSeed("cartagena", "Tour Gastronómico Costeño", "Plan en Cartagena para disfrutar Tour Gastronómico Costeño con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 75000.0, 120, 4.3, "Gastronomía", "/assets/actividades/cartagena/Tour%20Gastrono%CC%81mico%20Costen%CC%83o.jpg"),
                new ActividadSeed("cartagena", "Tour Nocturno De Leyendas", "Plan en Cartagena para disfrutar Tour Nocturno De Leyendas con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 58000.0, 120, 4.3, "Experiencias auténticas", "/assets/actividades/cartagena/Tour%20Nocturno%20De%20Leyendas.jpg"),
                new ActividadSeed("medellin", "Atardecer Desde Terraza", "Plan en Medellín para disfrutar Atardecer Desde Terraza con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 55000.0, 120, 4.2, "Experiencia local", "/assets/actividades/medellin/Atardecer%20Desde%20Terraza.jpg"),
                new ActividadSeed("medellin", "Caminata Urbana Por Barrios Históricos", "Plan en Medellín para disfrutar Caminata Urbana Por Barrios Históricos con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 92000.0, 240, 4.5, "Naturaleza y aventura", "/assets/actividades/medellin/Caminata%20Urbana%20Por%20Barrios%20Histo%CC%81ricos.jpg"),
                new ActividadSeed("medellin", "Clase De Café Especial", "Plan en Medellín para disfrutar Clase De Café Especial con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 65000.0, 120, 4.6, "Gastronomía", "/assets/actividades/medellin/Clase%20De%20Cafe%CC%81%20Especial.jpg"),
                new ActividadSeed("medellin", "Clase De Cocina Antioqueña", "Plan en Medellín para disfrutar Clase De Cocina Antioqueña con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 70000.0, 120, 4.2, "Gastronomía", "/assets/actividades/medellin/Clase%20De%20Cocina%20Antioquen%CC%83a.jpg"),
                new ActividadSeed("medellin", "Clase De Salsa", "Plan en Medellín para disfrutar Clase De Salsa con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 70000.0, 180, 4.0, "Vida nocturna", "/assets/actividades/medellin/Clase%20De%20Salsa.jpg"),
                new ActividadSeed("medellin", "Escapada A Pueblo Cercano", "Plan en Medellín para disfrutar Escapada A Pueblo Cercano con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 60000.0, 120, 4.4, "Experiencias auténticas", "/assets/actividades/medellin/Escapada%20A%20Pueblo%20Cercano.jpg"),
                new ActividadSeed("medellin", "Excursión A Cascadas", "Plan en Medellín para disfrutar Excursión A Cascadas con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 75000.0, 240, 4.5, "Naturaleza y aventura", "/assets/actividades/medellin/Excursio%CC%81n%20A%20Cascadas.jpg"),
                new ActividadSeed("medellin", "Experiencia Cultural Local", "Plan en Medellín para disfrutar Experiencia Cultural Local con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 48000.0, 120, 4.6, "Cultura e historia", "/assets/actividades/medellin/Experiencia%20Cultural%20Local.jpg"),
                new ActividadSeed("medellin", "Mercado De Diseño Local", "Plan en Medellín para disfrutar Mercado De Diseño Local con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 82000.0, 120, 4.3, "Gastronomía", "/assets/actividades/medellin/Mercado%20De%20Disen%CC%83o%20Local.jpg"),
                new ActividadSeed("medellin", "Parapente Cerca De Medellín", "Plan en Medellín para disfrutar Parapente Cerca De Medellín con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 48000.0, 120, 4.6, "Experiencia local", "/assets/actividades/medellin/Parapente%20Cerca%20De%20Medelli%CC%81n.jpg"),
                new ActividadSeed("medellin", "Picnic En Zona Verde", "Plan en Medellín para disfrutar Picnic En Zona Verde con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 55000.0, 120, 4.6, "Relax", "/assets/actividades/medellin/Picnic%20En%20Zona%20Verde.jpg"),
                new ActividadSeed("medellin", "Recorrido De Grafiti", "Plan en Medellín para disfrutar Recorrido De Grafiti con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 55000.0, 120, 4.1, "Experiencias auténticas", "/assets/actividades/medellin/Recorrido%20De%20Grafiti.jpg"),
                new ActividadSeed("medellin", "Recorrido Nocturno Por Provenza", "Plan en Medellín para disfrutar Recorrido Nocturno Por Provenza con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 50000.0, 120, 4.2, "Experiencias auténticas", "/assets/actividades/medellin/Recorrido%20Nocturno%20Por%20Provenza.jpg"),
                new ActividadSeed("medellin", "Ruta Gastronómica Paisa", "Plan en Medellín para disfrutar Ruta Gastronómica Paisa con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 82000.0, 120, 4.3, "Gastronomía", "/assets/actividades/medellin/Ruta%20Gastrono%CC%81mica%20Paisa.jpg"),
                new ActividadSeed("medellin", "Senderismo En Parque Natural", "Plan en Medellín para disfrutar Senderismo En Parque Natural con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 92000.0, 240, 4.5, "Naturaleza y aventura", "/assets/actividades/medellin/Senderismo%20En%20Parque%20Natural.jpg"),
                new ActividadSeed("medellin", "Show De Música En Vivo", "Plan en Medellín para disfrutar Show De Música En Vivo con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 85000.0, 180, 4.3, "Vida nocturna", "/assets/actividades/medellin/Show%20De%20Mu%CC%81sica%20En%20Vivo.jpg"),
                new ActividadSeed("medellin", "Tour De Transformación Social", "Plan en Medellín para disfrutar Tour De Transformación Social con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 60000.0, 120, 4.2, "Experiencias auténticas", "/assets/actividades/medellin/Tour%20De%20Transformacio%CC%81n%20Social.jpg"),
                new ActividadSeed("medellin", "Tour Fotográfico Urbano", "Plan en Medellín para disfrutar Tour Fotográfico Urbano con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 58000.0, 120, 4.2, "Experiencias auténticas", "/assets/actividades/medellin/Tour%20Fotogra%CC%81fico%20Urbano.jpg"),
                new ActividadSeed("medellin", "Viaje En Metrocable", "Plan en Medellín para disfrutar Viaje En Metrocable con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 55000.0, 120, 4.1, "Experiencias auténticas", "/assets/actividades/medellin/Viaje%20En%20Metrocable.jpg"),
                new ActividadSeed("medellin", "Visita A Finca Cafetera", "Plan en Medellín para disfrutar Visita A Finca Cafetera con buen ritmo y seguridad. Ideal para sumarlo al itinerario del grupo.", 70000.0, 120, 4.1, "Gastronomía", "/assets/actividades/medellin/Visita%20A%20Finca%20Cafetera.jpg")
        );

        List<Actividad> actividades = new ArrayList<>();
        for (ActividadSeed seed : seeds) {
            Actividad actividad = new Actividad(seed.nombre(), seed.descripcion(), seed.costoPorPersona(), seed.duracionMin());
            actividad.setCalificacionPromedio(seed.calificacionPromedio());
            actividad.setVigenciaInicio(LocalDate.of(2026, 1, 1));
            actividad.setVigenciaFin(LocalDate.of(2026, 12, 31));
            actividad.setFuente("assets-backend");
            actividad.setCiudad(ciudades.get(seed.destinoKey()));
            actividad.setUbicacion(ubicaciones.get(seed.destinoKey()));
            actividad.setImagenUrl(seed.imagenUrl());

            Categoria categoria = categorias.getOrDefault(seed.categoria(), categorias.get("Experiencia local"));
            actividad.setCategorias(new HashSet<>(Set.of(categoria)));

            Imagen imagen = new Imagen(seed.imagenUrl(), true, actividad);
            actividad.getImagenes().add(imagen);
            actividades.add(actividadRepository.save(actividad));
        }
        return actividades;
    }

    private Perfil crearPerfil(
            Usuario usuario,
            GrupoViaje grupo,
            Double presupuesto,
            Integer personasCargo,
            Integer tiempoDiario,
            boolean faseIndividualLista,
            boolean participaEnCoordinacion,
            Set<Categoria> categoriasPreferidas
    ) {
        Perfil perfil = new Perfil(presupuesto, personasCargo, tiempoDiario, new HashSet<>(categoriasPreferidas));
        perfil.setUsuario(usuario);
        perfil.setGrupoViaje(grupo);
        perfil.setFaseIndividualLista(faseIndividualLista);
        perfil.setParticipaEnCoordinacion(participaEnCoordinacion);
        return perfil;
    }

    private void seleccionarPorIndice(Perfil perfil, List<Actividad> actividades, int... indices) {
        for (int index : indices) {
            if (index < 0 || index >= actividades.size()) continue;
            Actividad actividad = actividades.get(index);
            perfil.getActividadesSeleccionadas().add(actividad);
            actividad.getPerfilesQueLaSeleccionaron().add(perfil);
        }
    }

    private record ActividadSeed(
            String destinoKey,
            String nombre,
            String descripcion,
            Double costoPorPersona,
            Integer duracionMin,
            Double calificacionPromedio,
            String categoria,
            String imagenUrl
    ) {}
}
