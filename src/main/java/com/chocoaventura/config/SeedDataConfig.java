package com.chocoaventura.config;

import com.chocoaventura.entities.*;
import com.chocoaventura.entities.enums.EstadoGrupoViaje;
import com.chocoaventura.repositories.*;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
            // =========================
            // 1. CATEGORÍAS BASE
            // =========================
            Categoria cultural = categoriaRepository.save(new Categoria("Cultural", "Planes culturales"));
            Categoria naturaleza = categoriaRepository.save(new Categoria("Naturaleza", "Planes de naturaleza"));
            Categoria acuatico = categoriaRepository.save(new Categoria("Acuático", "Planes en agua o recorridos acuáticos"));
            Categoria aventura = categoriaRepository.save(new Categoria("Aventura", "Planes de aventura"));
            Categoria gastronomico = categoriaRepository.save(new Categoria("Gastronómico", "Planes de comida y degustación"));

            // =========================
            // 2. USUARIOS
            // =========================
            Usuario laura = usuarioRepository.save(new Usuario("Laura", "laura@choco.com", "123456"));
            Usuario cata = usuarioRepository.save(new Usuario("Cata", "cata@choco.com", "123456"));
            Usuario michelle = usuarioRepository.save(new Usuario("Michelle", "michelle@choco.com", "123456"));
            Usuario fer = usuarioRepository.save(new Usuario("Fernando", "fernando@choco.com", "123456"));
            Usuario sara = usuarioRepository.save(new Usuario("Sara", "sara@choco.com", "123456"));
            Usuario juan = usuarioRepository.save(new Usuario("Juan", "juan@choco.com", "123456")); // tardío

            // =========================
            // 3. CIUDAD Y UBICACIONES
            // =========================
            Ciudad medellin = ciudadRepository.save(new Ciudad("Medellín", "Colombia"));

            Ubicacion hotelCentro = ubicacionRepository.save(
                    new Ubicacion("Hotel Centro", "Centro de Medellín", 6.2442, -75.5812)
            );
            Ubicacion museoZona = ubicacionRepository.save(
                    new Ubicacion("Zona Museos", "La Candelaria", 6.2518, -75.5636)
            );
            Ubicacion rioZona = ubicacionRepository.save(
                    new Ubicacion("Zona Río", "Puerto / muelle turístico", 6.2300, -75.5700)
            );
            Ubicacion naturalezaZona = ubicacionRepository.save(
                    new Ubicacion("Zona Verde", "Senderos y miradores", 6.2700, -75.5600)
            );
            Ubicacion comidaZona = ubicacionRepository.save(
                    new Ubicacion("Zona Gastronómica", "El Poblado", 6.2090, -75.5670)
            );

            // =========================
            // 4. ACTIVIDADES
            // =========================
            Actividad museoArte = crearActividad(
                    "Museo de Arte Moderno",
                    "Museo con exposiciones temporales y recorrido cultural",
                    30000.0,
                    120,
                    4.6,
                    medellin,
                    museoZona,
                    Set.of(cultural)
            );

            Actividad museoHistorico = crearActividad(
                    "Museo Histórico de la Ciudad",
                    "Recorrido por historia local y salas patrimoniales",
                    25000.0,
                    110,
                    4.5,
                    medellin,
                    museoZona,
                    Set.of(cultural)
            );

            Actividad galeriaCentro = crearActividad(
                    "Galería y centro cultural",
                    "Exposición artística y visita guiada",
                    28000.0,
                    100,
                    4.3,
                    medellin,
                    museoZona,
                    Set.of(cultural)
            );

            Actividad paseoBote = crearActividad(
                    "Paseo en bote",
                    "Recorrido por el agua con guía local",
                    55000.0,
                    90,
                    4.7,
                    medellin,
                    rioZona,
                    Set.of(acuatico, aventura)
            );

            Actividad recorridoAcuatico = crearActividad(
                    "Recorrido acuático",
                    "Salida en embarcación con paisaje y explicación cultural",
                    50000.0,
                    95,
                    4.6,
                    medellin,
                    rioZona,
                    Set.of(acuatico)
            );

            Actividad tourLancha = crearActividad(
                    "Tour en lancha",
                    "Trayecto turístico en lancha por zona de agua",
                    60000.0,
                    100,
                    4.8,
                    medellin,
                    rioZona,
                    Set.of(acuatico, aventura)
            );

            Actividad senderoEcologico = crearActividad(
                    "Sendero ecológico",
                    "Caminata suave por sendero natural",
                    20000.0,
                    150,
                    4.4,
                    medellin,
                    naturalezaZona,
                    Set.of(naturaleza)
            );

            Actividad rutaNatural = crearActividad(
                    "Ruta natural guiada",
                    "Recorrido ecológico con observación de paisaje",
                    22000.0,
                    140,
                    4.5,
                    medellin,
                    naturalezaZona,
                    Set.of(naturaleza)
            );

            Actividad caminataMirador = crearActividad(
                    "Caminata al mirador",
                    "Subida ligera con vista panorámica",
                    18000.0,
                    130,
                    4.2,
                    medellin,
                    naturalezaZona,
                    Set.of(naturaleza, aventura)
            );

            Actividad degustacionCafe = crearActividad(
                    "Degustación de café",
                    "Experiencia guiada de café local",
                    35000.0,
                    80,
                    4.6,
                    medellin,
                    comidaZona,
                    Set.of(gastronomico, cultural)
            );

            Actividad tourComida = crearActividad(
                    "Tour gastronómico",
                    "Recorrido por sabores tradicionales",
                    45000.0,
                    120,
                    4.7,
                    medellin,
                    comidaZona,
                    Set.of(gastronomico)
            );

            Actividad cenaLocal = crearActividad(
                    "Cena de cocina local",
                    "Experiencia culinaria con menú típico",
                    50000.0,
                    100,
                    4.5,
                    medellin,
                    comidaZona,
                    Set.of(gastronomico)
            );

            Actividad canopy = crearActividad(
                    "Canopy entre montañas",
                    "Plan de aventura con cable aéreo",
                    70000.0,
                    90,
                    4.8,
                    medellin,
                    naturalezaZona,
                    Set.of(aventura)
            );

            Actividad escalada = crearActividad(
                    "Escalada guiada",
                    "Actividad de roca con instructor",
                    65000.0,
                    100,
                    4.4,
                    medellin,
                    naturalezaZona,
                    Set.of(aventura)
            );

            actividadRepository.saveAll(List.of(
                    museoArte, museoHistorico, galeriaCentro,
                    paseoBote, recorridoAcuatico, tourLancha,
                    senderoEcologico, rutaNatural, caminataMirador,
                    degustacionCafe, tourComida, cenaLocal,
                    canopy, escalada
            ));

            // =========================
            // 5. GRUPO DE VIAJE
            // =========================
            GrupoViaje grupo = new GrupoViaje(
                    "Viaje ChocoAventura Medellín",
                    "Viaje grupal para probar exploración grupal",
                    LocalTime.of(8, 0),
                    LocalTime.of(13, 0),
                    60,
                    LocalDateTime.of(2026, 4, 10, 9, 0),
                    LocalDateTime.of(2026, 4, 20, 18, 0),
                    medellin,
                    laura
            );
            grupo.setEstadia(hotelCentro);
            grupo.setEstado(EstadoGrupoViaje.CONFIRMACION_GRUPAL_PENDIENTE);

            grupo = grupoViajeRepository.save(grupo);

            // =========================
            // 6. PERFILES DECISORES
            // =========================
            Perfil perfilLaura = crearPerfil(laura, grupo, 500000.0, 0, 360, true, true,
                    Set.of(cultural, gastronomico));
            Perfil perfilCata = crearPerfil(cata, grupo, 400000.0, 0, 300, true, true,
                    Set.of(acuatico, aventura));
            Perfil perfilMichelle = crearPerfil(michelle, grupo, 450000.0, 0, 320, true, true,
                    Set.of(naturaleza, cultural));
            Perfil perfilFer = crearPerfil(fer, grupo, 380000.0, 0, 280, true, true,
                    Set.of(gastronomico, naturaleza));
            Perfil perfilSara = crearPerfil(sara, grupo, 350000.0, 0, 250, true, true,
                    Set.of(cultural));

            // Participante tardío: se puede unir, pero ya no participa en coordinación
            Perfil perfilJuan = crearPerfil(juan, grupo, 420000.0, 0, 300, true, false,
                    Set.of(aventura));

            // =========================
            // 7. SELECCIONES INDIVIDUALES
            // =========================
            seleccionar(perfilLaura, museoArte, museoHistorico, degustacionCafe, tourComida);
            seleccionar(perfilCata, paseoBote, recorridoAcuatico, tourLancha, canopy);
            seleccionar(perfilMichelle, museoArte, senderoEcologico, rutaNatural, caminataMirador);
            seleccionar(perfilFer, tourComida, cenaLocal, senderoEcologico, rutaNatural);
            // Sara terminó fase individual, pero no seleccionó ninguna actividad
            // Juan no participa en coordinación, aunque está en el viaje

            perfilRepository.saveAll(List.of(
                    perfilLaura, perfilCata, perfilMichelle, perfilFer, perfilSara, perfilJuan
            ));
        };
    }

    private Actividad crearActividad(
            String nombre,
            String descripcion,
            Double costo,
            Integer duracion,
            Double rating,
            Ciudad ciudad,
            Ubicacion ubicacion,
            Set<Categoria> categorias
    ) {
        Actividad actividad = new Actividad(nombre, descripcion, costo, duracion);
        actividad.setCalificacionPromedio(rating);
        actividad.setCiudad(ciudad);
        actividad.setUbicacion(ubicacion);
        actividad.setCategorias(new HashSet<>(categorias));
        actividad.setVigenciaInicio(LocalDate.of(2026, 1, 1));
        actividad.setVigenciaFin(LocalDate.of(2026, 12, 31));
        actividad.setFuente("seed");
        return actividad;
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

    private void seleccionar(Perfil perfil, Actividad... actividades) {
        for (Actividad actividad : actividades) {
            perfil.getActividadesSeleccionadas().add(actividad);
            actividad.getPerfilesQueLaSeleccionaron().add(perfil);
        }
    }
}