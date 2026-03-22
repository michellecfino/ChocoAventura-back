package com.chocoaventura.Services;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.Repositories.ActividadRepository;
import com.chocoaventura.Repositories.PerfilRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class PerfilService {

    @Autowired
    private PerfilRepository perfilRepository;

    @Autowired
    private ActividadRepository actividadRepository;

    // =========================
    // CRUD básico
    // =========================

    public Perfil create(Perfil perfil) {
        return perfilRepository.save(perfil);
    }

    public List<Perfil> getAll() {
        return perfilRepository.findAll();
    }

    public Perfil getById(Long id) {
        return perfilRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + id));
    }

    public Perfil update(Long id, Perfil datos) {
        Perfil perfil = perfilRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + id));

        perfil.setPresupuesto(datos.getPresupuesto());
        perfil.setPersonasCargo(datos.getPersonasCargo());
        perfil.setTiempoDiarioActividades(datos.getTiempoDiarioActividades());
        perfil.setFaseIndividualLista(datos.getFaseIndividualLista());
        perfil.setCodigoVuelo(datos.getCodigoVuelo());
        perfil.setCategoriasPreferidas(datos.getCategoriasPreferidas());
        perfil.setActividadesSeleccionadas(datos.getActividadesSeleccionadas());
        perfil.setUsuario(datos.getUsuario());
        perfil.setGrupoViaje(datos.getGrupoViaje());

        return perfilRepository.save(perfil);
    }

    public void delete(Long id) {
        Perfil perfil = perfilRepository.findById(id).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + id));
        perfilRepository.delete(perfil);
    }

    // =========================
    // Lógica
    // =========================

    public void marcarFaseIndividualLista(Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + perfilId));
        perfil.setFaseIndividualLista(true);
        perfilRepository.save(perfil);
    }

    public void seleccionarActividad(Long perfilId, Long actividadId) {
        Perfil perfil = perfilRepository.findById(perfilId).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + perfilId));
        Actividad actividad = actividadRepository.findById(actividadId).orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con id: " + actividadId));

        perfil.getActividadesSeleccionadas().add(actividad);
        perfilRepository.save(perfil);
    }

    public void quitarActividadSeleccionada(Long perfilId, Long actividadId) {
        Perfil perfil = perfilRepository.findById(perfilId).orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado con id: " + perfilId));
        Actividad actividad = actividadRepository.findById(actividadId).orElseThrow(() -> new EntityNotFoundException("Actividad no encontrada con id: " + actividadId));

        perfil.getActividadesSeleccionadas().remove(actividad);
        perfilRepository.save(perfil);
    }
}