package com.web.clinica.controller;

import com.web.clinica.dto.request.DisponibilidadBaseCreateRequest;
import com.web.clinica.dto.request.ExcepcionDisponibilidadCreateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.DisponibilidadBaseResponse;
import com.web.clinica.dto.response.ExcepcionDisponibilidadResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IDisponibilidadService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/disponibilidad")
@RequiredArgsConstructor
public class DisponibilidadController {

    private final IDisponibilidadService disponibilidadService;

    @GetMapping("/doctor/{doctorId}/base")
    @RequierePermiso({"disponibilidad.ver_propia", "disponibilidad.ver_todas"})
    public List<DisponibilidadBaseResponse> listarBases(@PathVariable Long doctorId) {
        return disponibilidadService.listarBases(doctorId);
    }

    @PostMapping("/doctor/{doctorId}/base")
    @RequierePermiso({"disponibilidad.editar_propia", "disponibilidad.editar_todas"})
    public ResponseEntity<DisponibilidadBaseResponse> guardarBase(@PathVariable Long doctorId,
                                                                  @Valid @RequestBody DisponibilidadBaseCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidadService.guardarBase(doctorId, solicitud));
    }

    @DeleteMapping("/doctor/{doctorId}/base/{id}")
    @RequierePermiso({"disponibilidad.editar_propia", "disponibilidad.editar_todas"})
    public ApiResponse eliminarBase(@PathVariable Long doctorId, @PathVariable Long id) {
        disponibilidadService.eliminarBase(doctorId, id);
        return new ApiResponse("Horario base eliminado correctamente", true);
    }

    @GetMapping("/doctor/{doctorId}/excepciones")
    @RequierePermiso({"disponibilidad.ver_propia", "disponibilidad.ver_todas"})
    public List<ExcepcionDisponibilidadResponse> listarExcepciones(
            @PathVariable Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin) {
        return disponibilidadService.listarExcepciones(doctorId, fechaInicio, fechaFin);
    }

    @PostMapping("/doctor/{doctorId}/excepciones")
    @RequierePermiso({"disponibilidad.editar_propia", "disponibilidad.editar_todas"})
    public ResponseEntity<ExcepcionDisponibilidadResponse> crearExcepcion(
            @PathVariable Long doctorId,
            @Valid @RequestBody ExcepcionDisponibilidadCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(disponibilidadService.crearExcepcion(doctorId, solicitud));
    }

    @DeleteMapping("/doctor/{doctorId}/excepciones/{id}")
    @RequierePermiso({"disponibilidad.editar_propia", "disponibilidad.editar_todas"})
    public ApiResponse eliminarExcepcion(@PathVariable Long doctorId, @PathVariable Long id) {
        disponibilidadService.eliminarExcepcion(doctorId, id);
        return new ApiResponse("Excepcion eliminada correctamente", true);
    }
}
