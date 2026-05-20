package com.chocoaventura.controllers;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.chocoaventura.dto.gastos.DetalleFinancieroDTO;
import com.chocoaventura.dto.gastos.GastoRegistradoDTO;
import com.chocoaventura.dto.gastos.RegistrarGastoRequestDTO;
import com.chocoaventura.dto.gastos.ViajeFinancieroDTO;
import com.chocoaventura.entities.Pago;
import com.chocoaventura.services.GastosFinancierosService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/gastos")
@CrossOrigin(origins = "*")
public class GastosController {

    private final GastosFinancierosService gastosFinancierosService;

    public GastosController(GastosFinancierosService gastosFinancierosService) {
        this.gastosFinancierosService = gastosFinancierosService;
    }

    @GetMapping("/usuarios/{usuarioId}/viajes")
    public List<ViajeFinancieroDTO> listarViajes(@PathVariable Long usuarioId) {
        return gastosFinancierosService.listarViajesPorUsuario(usuarioId);
    }

    @GetMapping("/grupos/{grupoId}/perfiles/{perfilId}")
    public DetalleFinancieroDTO detalle(
            @PathVariable Long grupoId,
            @PathVariable Long perfilId) {
        return gastosFinancierosService.detallePorGrupoYPerfil(grupoId, perfilId);
    }

    @PostMapping("/registrar")
    public ResponseEntity<GastoRegistradoDTO> registrar(@Valid @RequestBody RegistrarGastoRequestDTO body) {
        Pago pago = gastosFinancierosService.registrarGasto(body);
        return ResponseEntity.ok(new GastoRegistradoDTO(pago.getId(), pago.getNombre(), pago.getMontoTotal()));
    }

    @PutMapping("/deudas/{deudaId}/saldar")
    public ResponseEntity<Void> saldar(@PathVariable Long deudaId) {
        gastosFinancierosService.saldarDeuda(deudaId);
        return ResponseEntity.noContent().build();
    }
}
