package com.chocoaventura.services;

import java.text.Normalizer;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.chocoaventura.dto.gastos.DetalleFinancieroDTO;
import com.chocoaventura.dto.gastos.GastoRecienteDTO;
import com.chocoaventura.dto.gastos.PersonaMontoDTO;
import com.chocoaventura.dto.gastos.RegistrarGastoRequestDTO;
import com.chocoaventura.dto.gastos.ViajeFinancieroDTO;
import com.chocoaventura.entities.Deuda;
import com.chocoaventura.entities.GrupoViaje;
import com.chocoaventura.entities.Pago;
import com.chocoaventura.entities.ParticipacionPago;
import com.chocoaventura.entities.Perfil;
import com.chocoaventura.repositories.DeudaRepository;
import com.chocoaventura.repositories.GrupoViajeRepository;
import com.chocoaventura.repositories.PagoRepository;
import com.chocoaventura.repositories.ParticipacionPagoRepository;
import com.chocoaventura.repositories.PerfilRepository;

import jakarta.persistence.EntityNotFoundException;

@Service
public class GastosFinancierosService {

    private final PerfilRepository perfilRepository;
    private final GrupoViajeRepository grupoViajeRepository;
    private final PagoRepository pagoRepository;
    private final ParticipacionPagoRepository participacionPagoRepository;
    private final DeudaRepository deudaRepository;

    public GastosFinancierosService(PerfilRepository perfilRepository,
            GrupoViajeRepository grupoViajeRepository,
            PagoRepository pagoRepository,
            ParticipacionPagoRepository participacionPagoRepository,
            DeudaRepository deudaRepository) {
        this.perfilRepository = perfilRepository;
        this.grupoViajeRepository = grupoViajeRepository;
        this.pagoRepository = pagoRepository;
        this.participacionPagoRepository = participacionPagoRepository;
        this.deudaRepository = deudaRepository;
    }

    public List<ViajeFinancieroDTO> listarViajesPorUsuario(Long usuarioId) {
        List<Perfil> perfiles = perfilRepository.findByUsuarioId(usuarioId);
        List<ViajeFinancieroDTO> lista = new ArrayList<>();
        for (Perfil perfil : perfiles) {
            GrupoViaje gv = perfil.getGrupoViaje();
            double hasGastado = calcularHasGastado(perfil);
            double tuDebes = sumarDeudasComoDeudor(perfil.getId());
            double teDeben = sumarDeudasComoAcreedor(perfil.getId());
            String estado = resolverEstado(tuDebes, teDeben);
            lista.add(new ViajeFinancieroDTO(
                    gv.getId(),
                    gv.getId(),
                    gv.getNombre(),
                    estado,
                    redondear(tuDebes),
                    redondear(teDeben),
                    redondear(hasGastado),
                    perfil.getPresupuesto(),
                    normalizarCiudad(gv.getCiudadDestino() == null ? null : gv.getCiudadDestino().getNombre()),
                    perfil.getId()));
        }
        lista.sort(Comparator
                .comparingInt((ViajeFinancieroDTO v) -> prioridadEstado(v.estado()))
                .thenComparing(ViajeFinancieroDTO::nombreViaje, String.CASE_INSENSITIVE_ORDER));
        return lista;
    }

    public DetalleFinancieroDTO detallePorGrupoYPerfil(Long grupoId, Long perfilId) {
        Perfil perfil = perfilRepository.findById(perfilId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado: " + perfilId));
        if (!perfil.getGrupoViaje().getId().equals(grupoId)) {
            throw new IllegalArgumentException("El perfil no pertenece al grupo indicado");
        }
        double presupuesto = perfil.getPresupuesto();
        double gastado = calcularHasGastado(perfil);
        double restante = Math.max(0, presupuesto - gastado);

        Map<Long, Double> tuDebesMap = new HashMap<>();
        for (Deuda d : deudaRepository.findByDeudorIdAndSaldadaFalse(perfilId)) {
            Long acr = d.getAcreedor().getId();
            tuDebesMap.merge(acr, d.getMonto(), Double::sum);
        }
        List<PersonaMontoDTO> personasTuDebes = tuDebesMap.entrySet().stream()
                .map(e -> new PersonaMontoDTO(e.getKey(), nombrePerfil(e.getKey()), redondear(e.getValue())))
                .sorted(Comparator.comparing(PersonaMontoDTO::nombre))
                .toList();

        Map<Long, Double> teDebenMap = new HashMap<>();
        for (Deuda d : deudaRepository.findByAcreedorIdAndSaldadaFalse(perfilId)) {
            Long deu = d.getDeudor().getId();
            teDebenMap.merge(deu, d.getMonto(), Double::sum);
        }
        List<PersonaMontoDTO> personasTeDeben = teDebenMap.entrySet().stream()
                .map(e -> new PersonaMontoDTO(e.getKey(), nombrePerfil(e.getKey()), redondear(e.getValue())))
                .sorted(Comparator.comparing(PersonaMontoDTO::nombre))
                .toList();

        boolean todoSaldado = personasTuDebes.isEmpty() && personasTeDeben.isEmpty();

        Map<String, Double> porCat = new HashMap<>();
        for (Pago p : pagoRepository.findByGrupoViajeId(grupoId)) {
            String cat = p.getCategoria() != null && !p.getCategoria().isBlank() ? p.getCategoria() : "Otros";
            porCat.merge(cat, p.getMontoTotal(), Double::sum);
        }

        List<GastoRecienteDTO> recientes = pagoRepository.findByGrupoViajeId(grupoId).stream()
                .sorted(Comparator.comparing(Pago::getFecha).reversed())
                .limit(4)
                .map(p -> new GastoRecienteDTO(
                        p.getId(),
                        p.getNombre(),
                        p.getMontoTotal(),
                        p.getFecha(),
                        p.getCategoria() != null ? p.getCategoria() : "Otros"))
                .toList();

        long deudasActivas = deudaRepository.findByDeudorIdAndSaldadaFalse(perfilId).size()
                + deudaRepository.findByAcreedorIdAndSaldadaFalse(perfilId).size();
        String recomendacion;
        if (todoSaldado) {
            recomendacion = "Todo saldado: cuentas claras, aventura feliz.";
        } else {
            int mov = (int) Math.max(1, Math.ceil(deudasActivas / 2.0));
            recomendacion = "Para saldar este viaje solo necesitas " + mov + " movimiento(s).";
        }

        return new DetalleFinancieroDTO(
                presupuesto,
                redondear(gastado),
                redondear(restante),
                personasTuDebes,
                personasTeDeben,
                porCat.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, e -> redondear(e.getValue()))),
                recientes,
                recomendacion,
                todoSaldado);
    }

    @Transactional
    public Pago registrarGasto(RegistrarGastoRequestDTO req) {
        Perfil perfil = perfilRepository.findById(req.perfilId())
                .orElseThrow(() -> new EntityNotFoundException("Perfil no encontrado"));
        if (!perfil.getGrupoViaje().getId().equals(req.grupoId())) {
            throw new IllegalArgumentException("El perfil no pertenece al grupo");
        }
        GrupoViaje grupo = grupoViajeRepository.findById(req.grupoId())
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado"));

        String tipo = req.tipo().toUpperCase();
        LocalDate hoy = LocalDate.now();
        Pago pago = new Pago(req.descripcion(), req.monto(), hoy, grupo);
        pago.setCategoria(normalizarCategoria(req.categoria()));
        pago.setTipoGasto(tipo);
        pago.setNota(req.nota());
        if (req.detalleDivision() != null && !req.detalleDivision().isBlank()) {
            pago.setDetalleDivision(req.detalleDivision());
        }
        pago = pagoRepository.save(pago);

        if ("INDIVIDUAL".equals(tipo)) {
            ParticipacionPago part = new ParticipacionPago(req.monto(), 100.0, perfil, pago);
            participacionPagoRepository.save(part);
            return pagoRepository.findById(pago.getId()).orElseThrow();
        }

        // COMPARTIDO
        Long pagadorId = req.pagadoPorPerfilId() != null ? req.pagadoPorPerfilId() : perfil.getId();
        Perfil pagador = perfilRepository.findById(pagadorId)
                .orElseThrow(() -> new EntityNotFoundException("Perfil pagador no encontrado"));
        if (!pagador.getGrupoViaje().getId().equals(grupo.getId())) {
            throw new IllegalArgumentException("El pagador no pertenece al grupo");
        }

        List<Perfil> participantes;
        if (req.participantesIds() == null || req.participantesIds().isEmpty()) {
            participantes = perfilRepository.findByGrupoViajeId(grupo.getId());
        } else {
            participantes = req.participantesIds().stream()
                    .map(id -> perfilRepository.findById(id).orElseThrow(
                            () -> new EntityNotFoundException("Participante no encontrado: " + id)))
                    .filter(p -> p.getGrupoViaje().getId().equals(grupo.getId()))
                    .toList();
        }
        if (participantes.isEmpty()) {
            throw new IllegalArgumentException("Debe haber al menos un participante");
        }

        int n = participantes.size();
        double porcentaje = 100.0 / n;
        double parte = req.monto() / n;

        for (Perfil p : participantes) {
            double montoPagado = Objects.equals(p.getId(), pagador.getId()) ? req.monto() : 0.0;
            ParticipacionPago part = new ParticipacionPago(montoPagado, porcentaje, p, pago);
            participacionPagoRepository.save(part);
        }

        for (Perfil p : participantes) {
            if (!Objects.equals(p.getId(), pagador.getId())) {
                Deuda d = new Deuda(parte, p, pagador, pago);
                deudaRepository.save(d);
            }
        }

        return pagoRepository.findById(pago.getId()).orElseThrow();
    }

    public void saldarDeuda(Long deudaId) {
        Deuda d = deudaRepository.findById(deudaId)
                .orElseThrow(() -> new EntityNotFoundException("Deuda no encontrada"));
        d.setSaldada(true);
        deudaRepository.save(d);
    }

    private double calcularHasGastado(Perfil perfil) {
        Long grupoId = perfil.getGrupoViaje().getId();
        double total = 0;
        for (Pago pago : pagoRepository.findByGrupoViajeId(grupoId)) {
            var opt = participacionPagoRepository.findByPerfilIdAndPagoId(perfil.getId(), pago.getId());
            if (opt.isPresent()) {
                ParticipacionPago part = opt.get();
                total += pago.getMontoTotal() * (part.getPorcentajeResponsabilidad() / 100.0);
            }
        }
        return total;
    }

    private double sumarDeudasComoDeudor(Long perfilId) {
        return deudaRepository.findByDeudorIdAndSaldadaFalse(perfilId).stream()
                .mapToDouble(Deuda::getMonto).sum();
    }

    private double sumarDeudasComoAcreedor(Long perfilId) {
        return deudaRepository.findByAcreedorIdAndSaldadaFalse(perfilId).stream()
                .mapToDouble(Deuda::getMonto).sum();
    }

    private String nombrePerfil(Long perfilId) {
        return perfilRepository.findById(perfilId)
                .map(p -> p.getUsuario().getNombre())
                .orElse("?");
    }

    private static String resolverEstado(double tuDebes, double teDeben) {
        boolean debe = tuDebes > 0.009;
        boolean leDeben = teDeben > 0.009;
        if (debe && leDeben) {
            return "PENDIENTE";
        }
        if (debe) {
            return "DEBES_PAGAR";
        }
        if (leDeben) {
            return "TE_DEBEN";
        }
        return "SALDADO";
    }

    private static int prioridadEstado(String estado) {
        return switch (estado) {
            case "DEBES_PAGAR" -> 0;
            case "TE_DEBEN" -> 1;
            case "PENDIENTE" -> 2;
            case "SALDADO" -> 3;
            default -> 4;
        };
    }

    private static double redondear(double v) {
        return Math.round(v * 100.0) / 100.0;
    }

    private static String normalizarCategoria(String categoria) {
        if (categoria == null || categoria.isBlank()) {
            return "Otros";
        }
        return categoria.trim();
    }

    private String normalizarCiudad(String value) {
        return Normalizer.normalize(Optional.ofNullable(value).orElse(""), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9 ]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }
}
