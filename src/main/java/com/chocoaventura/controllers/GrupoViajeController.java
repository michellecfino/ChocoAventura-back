package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.chocoaventura.DTOs.ConfirmarCoordinacionRequestDTO;
import com.chocoaventura.DTOs.CrearGrupoDTO;
import com.chocoaventura.DTOs.EstadoExploracionGrupalDTO;
import com.chocoaventura.DTOs.ExploracionGrupalResponseDTO;
import com.chocoaventura.DTOs.GrupoViajeFlutterDTO;
import com.chocoaventura.DTOs.UnirseGrupoDTO;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.services.ExploracionGrupalService;
import com.chocoaventura.services.GrupoViajeService;

@RestController
@RequestMapping("/grupos")
@CrossOrigin(origins = "*")
public class GrupoViajeController {

    @Autowired
    private GrupoViajeService grupoViajeService;

    @Autowired
    private ExploracionGrupalService exploracionGrupalService;

    // -------------------------------------------------------
    // CRUD básico (uso interno / admin)
    // -------------------------------------------------------

    @PostMapping
    public ResponseEntity<GrupoViaje> create(@RequestBody GrupoViaje grupoViaje) {
        return ResponseEntity.ok(grupoViajeService.create(grupoViaje));
    }

    @GetMapping
    public List<GrupoViaje> getAll() {
        return grupoViajeService.getAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<GrupoViajeFlutterDTO> getById(@PathVariable Long id) {
        GrupoViaje grupo = grupoViajeService.getById(id);
        return ResponseEntity.ok(GrupoViajeFlutterDTO.fromEntity(grupo));
    }

    @PutMapping("/{id}")
    public ResponseEntity<GrupoViaje> update(@PathVariable Long id, @RequestBody GrupoViaje grupoViaje) {
        return ResponseEntity.ok(grupoViajeService.update(id, grupoViaje));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<String> delete(@PathVariable Long id) {
        grupoViajeService.delete(id);
        return ResponseEntity.ok("Grupo eliminado correctamente");
    }

    // -------------------------------------------------------
    // ENDPOINTS CONSUMIDOS POR FLUTTER
    // -------------------------------------------------------

    /**
     * Crear grupo de viaje.
     * Flutter envía CrearGrupoDTO y espera GrupoViajeFlutterDTO.
     * Pantalla: CreacionGrupoViaje.dart
     */
    @PostMapping("/crear")
    public ResponseEntity<GrupoViajeFlutterDTO> crearGrupo(@RequestBody CrearGrupoDTO dto) {
        GrupoViaje grupo = grupoViajeService.crearGrupoViaje(dto);
        return ResponseEntity.ok(grupoViajeService.toFlutterDTOForUsuario(grupo, dto.getDuenoId()));
    }

    /**
     * Unirse a un grupo via código de invitación.
     * Flutter envía UnirseGrupoDTO desde: viajes_service.dart → unirseAGrupo().
     * Responde con texto plano que el front lee como String.
     */
    @PostMapping("/unirse")
    public ResponseEntity<GrupoViajeFlutterDTO> unirse(@RequestBody UnirseGrupoDTO dto) {
        GrupoViaje grupo = grupoViajeService.unirseAGrupoViaje(dto);
        return ResponseEntity.ok(grupoViajeService.toFlutterDTOForUsuario(grupo, dto.getUsuarioId()));
    }

    /**
     * Validar código antes de pedir presupuesto/categorías.
     * Flutter llama: GET /grupos/codigo/{codigo}
     */
    @GetMapping("/codigo/{codigo}")
    public ResponseEntity<GrupoViajeFlutterDTO> validarCodigo(@PathVariable String codigo) {
        GrupoViaje grupo = grupoViajeService.buscarPorCodigoInvitacion(codigo);
        return ResponseEntity.ok(GrupoViajeFlutterDTO.fromEntity(grupo));
    }

    /**
     * Viajes del usuario — formato adaptado al modelo GrupoViajeModel de Flutter.
     * Flutter llama: GET /grupos/usuarios/{usuarioId}
     * Retorna los grupos donde el usuario tiene perfil, con campos que Flutter necesita:
     * nombreViaje, destinoKey, destinoNombre, ciudadDepartamento, fechaInicio, fechaFin,
     * participantes, codigoInvitacion, linkInvitacion, faseActual, estadoDisplay.
     */
    @GetMapping("/usuarios/{usuarioId}")
    public ResponseEntity<List<GrupoViajeFlutterDTO>> getGruposPorUsuario(@PathVariable Long usuarioId) {
        List<GrupoViajeFlutterDTO> grupos = grupoViajeService.listarGruposPorUsuario(usuarioId);
        return ResponseEntity.ok(grupos);
    }

    /**
     * Detalle de un grupo — misma estructura que GrupoViajeModel de Flutter.
     * Flutter lo usará para la pantalla de detalle de viaje y el hub de itinerario.
     */
    @GetMapping("/{grupoId}/detalle")
    public ResponseEntity<GrupoViajeFlutterDTO> getDetalle(@PathVariable Long grupoId) {
        GrupoViaje grupo = grupoViajeService.getById(grupoId);
        return ResponseEntity.ok(GrupoViajeFlutterDTO.fromEntity(grupo));
    }

    /**
     * Link de invitación para compartir el grupo.
     * Flutter: feed_screen.dart y CreacionGrupoViaje.dart lo muestran al usuario.
     */
    @GetMapping("/{grupoId}/invitacion")
    public ResponseEntity<String> generarLink(@PathVariable Long grupoId) {
        String link = grupoViajeService.generarLinkInvitacion(grupoId);
        return ResponseEntity.ok(link);
    }

    // -------------------------------------------------------
    // EXPLORACIÓN GRUPAL (coordinación de actividades)
    // -------------------------------------------------------

    @GetMapping("/{grupoId}/exploracion-grupal/estado")
    public ResponseEntity<EstadoExploracionGrupalDTO> obtenerEstadoExploracionGrupal(
            @PathVariable Long grupoId,
            @RequestParam(required = false) Long usuarioId) {
        return ResponseEntity.ok(exploracionGrupalService.evaluarEstado(grupoId, usuarioId));
    }

    @PostMapping("/{grupoId}/exploracion-grupal/confirmar")
    public ResponseEntity<EstadoExploracionGrupalDTO> confirmarInicioCoordinacion(
            @PathVariable Long grupoId,
            @RequestBody ConfirmarCoordinacionRequestDTO dto) {
        if (dto.getUsuarioId() != null || dto.getActividadesInteresIds() != null) {
            return ResponseEntity.ok(
                    exploracionGrupalService.registrarExploracionIndividual(
                            grupoId, dto.getUsuarioId(), dto.getActividadesInteresIds()));
        }
        return ResponseEntity.ok(
                exploracionGrupalService.confirmarInicioCoordinacion(
                        grupoId, dto.getDuenoId(), Boolean.TRUE.equals(dto.getConfirmar())));
    }

    @GetMapping("/{grupoId}/exploracion-grupal")
    public ResponseEntity<ExploracionGrupalResponseDTO> obtenerExploracionGrupal(
            @PathVariable Long grupoId) {
        return ResponseEntity.ok(exploracionGrupalService.obtenerExploracionGrupal(grupoId));
    }
}
