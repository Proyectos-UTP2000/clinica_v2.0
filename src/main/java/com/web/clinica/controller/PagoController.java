package com.web.clinica.controller;

import com.web.clinica.dto.request.PagoCreateRequest;
import com.web.clinica.dto.response.PagoResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IPagoService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/pagos")
@RequiredArgsConstructor
public class PagoController {

    private final IPagoService pagoService;

    /** Registra un pago manual. */
    @PostMapping
    @RequierePermiso("pagos.crear")
    public ResponseEntity<PagoResponse> registrarPago(@Valid @RequestBody PagoCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pagoService.registrarPago(solicitud));
    }

    /** Obtiene el pago de una cita. */
    @GetMapping("/cita/{citaId}")
    @RequierePermiso("pagos.ver")
    public PagoResponse obtenerPorCita(@PathVariable Long citaId) {
        return pagoService.obtenerPorCita(citaId);
    }

    /** Lista pagos de un paciente. */
    @GetMapping("/paciente/{pacienteId}")
    @RequierePermiso("pagos.ver")
    public List<PagoResponse> listarPorPaciente(@PathVariable Long pacienteId) {
        return pagoService.listarPorPaciente(pacienteId);
    }

    /** Lista pagos vinculados a una caja diaria. */
    @GetMapping("/caja/{cajaId}")
    @RequierePermiso("caja.ver")
    public List<PagoResponse> listarPorCaja(@PathVariable Long cajaId) {
        return pagoService.listarPorCaja(cajaId);
    }
}
