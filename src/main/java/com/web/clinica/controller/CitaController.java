package com.web.clinica.controller;

import com.web.clinica.dto.request.CitaCreateRequest;
import com.web.clinica.dto.request.CitaUpdateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.CitaResponse;
import com.web.clinica.dto.response.DisponibilidadSlotResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.ICitaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citas")
@RequiredArgsConstructor
public class CitaController {

    private final ICitaService citaService;

    /** Lista citas internas con filtros opcionales. */
    @GetMapping
    @RequierePermiso("citas.ver_todas")
    public Page<CitaResponse> listar(@RequestParam(required = false) Long pacienteId,
                                     @RequestParam(required = false) Long doctorId,
                                     @RequestParam(required = false) Long sedeId,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                     @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                     Pageable pageable) {
        return citaService.listarConFiltros(pacienteId, doctorId, sedeId, fecha, fechaInicio, fechaFin, pageable);
    }

    /** Lista citas del doctor autenticado. */
    @GetMapping("/doctor")
    @RequierePermiso("citas.ver_propias")
    public Page<CitaResponse> listarMisCitas(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
                                             @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
                                             Pageable pageable) {
        return citaService.listarMisCitas(pageable, fecha, fechaInicio, fechaFin);
    }

    /** Obtiene una cita por id. */
    @GetMapping("/{id}")
    @RequierePermiso({"citas.ver_todas", "citas.ver_propias", "citas.ver_asignados"})
    public CitaResponse obtener(@PathVariable Long id) {
        return citaService.obtenerPorId(id);
    }

    /** Agenda una cita nueva. */
    @PostMapping
    @RequierePermiso("citas.crear")
    public ResponseEntity<CitaResponse> crear(@Valid @RequestBody CitaCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.crear(solicitud));
    }

    /** Reprograma una cita existente. */
    @PutMapping("/{id}/reprogramar")
    @RequierePermiso({"citas.editar_propias", "citas.editar_asignados"})
    public CitaResponse reprogramar(@PathVariable Long id, @RequestBody CitaUpdateRequest solicitud) {
        if (solicitud.getNuevaFechaHora() == null) {
            throw new BadRequestException("Debe indicar la nueva fecha y hora");
        }
        return citaService.reprogramar(id, solicitud.getNuevaFechaHora());
    }

    /** Cancela una cita vigente. */
    @PutMapping("/{id}/cancelar")
    @RequierePermiso("citas.cancelar")
    public ApiResponse cancelar(@PathVariable Long id) {
        citaService.cancelar(id);
        return new ApiResponse("Cita cancelada correctamente", true);
    }

    /** Devuelve slots disponibles de 30 minutos. */
    @GetMapping("/slots-disponibles")
    @RequierePermiso("citas.crear")
    public List<DisponibilidadSlotResponse> obtenerSlotsDisponibles(@RequestParam Long doctorId,
                                                                    @RequestParam Long sedeId,
                                                                    @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return citaService.obtenerSlotsDisponibles(doctorId, sedeId, fecha);
    }
}
