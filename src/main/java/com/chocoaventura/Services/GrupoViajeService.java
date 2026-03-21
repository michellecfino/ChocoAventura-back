package com.chocoaventura.Services;

import org.springframework.stereotype.Service;

import com.chocoaventura.entities.Actividad;
import com.chocoaventura.entities.Usuario;

@Service
public class GrupoViajeService {
    public void crearGrupoViaje() {
        // Lógica para crear un grupo de viaje
    }

    public void unirseAGrupoViaje() {
        // Lógica para unirse a un grupo de viaje
    }

    public void invitarUsuarioAGrupoViaje() {
        // Lógica para invitar a un usuario a un grupo de viaje
    }   

    public void invitarnuevoUsuarioAGrupoViaje() {
        // Lógica para invitar a un nuevo usuario a un grupo de viaje
    }   

    public void registrarPago() {
        // Lógica para registrar un pago en un grupo de viaje
    }   

    public void hacerSubasta() {
        // Lógica para realizar una subasta en un grupo de viaje
    }   

    public void hacerItinerario() {
        // Lógica para crear un itinerario en un grupo de viaje
    }   

    public void votarActividad(Usuario usuario, Actividad actividad) {
        // Lógica para votar por una actividad en un grupo de viaje
    }

    public void obtenerListaActividadesVotadas() {
        // Lógica para obtener la lista de actividades votadas en un grupo de viaje
    }

    public void hacerseleccionDeActividadesParaSubasta() {
        // Lógica para hacer la selección de actividades para una subasta en un grupo de viaje
        // estoo es teniedno en cuenta las actividades seleccionadas por cada usuario y haciendo una simp´lificacion para asi tener una lista de actividades más pequeña para la subasta 
        // con el algoritmo que definimos para tener cllaro como se haran esas selecciones de actividades para la subasta
        // Debe depender de una cantidad de días y el presupuesto de cada usuario para asi hacer una selección de actividades más justa para la subasta
    }


    public void SeleccionarActividadesFinales() {
        // Lógica para seleccionar las actividades finales para el grupo de viaje después de la subasta
    }

    public void obtenerItinerarioFinal() {
        // Lógica para obtener el itinerario final del grupo de viaje después de la selección de actividades finales
    }

    


}
