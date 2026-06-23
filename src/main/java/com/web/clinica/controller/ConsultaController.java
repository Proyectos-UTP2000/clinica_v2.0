package com.web.clinica.controller;

import com.web.clinica.dto.request.ConsultaCreateRequest;
import com.web.clinica.dto.request.NotaEvolucionRequest;
import com.web.clinica.dto.response.AdjuntoResponse;
import com.web.clinica.dto.response.ConsultaResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IHistorialService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final IHistorialService historialService;

    /** Crea una consulta clinica. */
    @PostMapping
    @RequierePermiso("historial.crear")
    public ResponseEntity<ConsultaResponse> crearConsulta(@Valid @RequestBody ConsultaCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historialService.crearConsulta(solicitud));
    }

    /** Obtiene una consulta con detalles. */
    @GetMapping("/{id}")
    @RequierePermiso({"historial.ver_todos", "historial.ver_propios", "historial.ver_basico"})
    public ConsultaResponse obtenerConsulta(@PathVariable Long id) {
        return historialService.obtenerConsulta(id);
    }

    /** Lista consultas de un paciente. */
    @GetMapping("/paciente/{pacienteId}")
    @RequierePermiso({"historial.ver_todos", "historial.ver_basico"})
    public Page<ConsultaResponse> listarPorPaciente(@PathVariable Long pacienteId, Pageable pageable) {
        return historialService.listarPorPaciente(pacienteId, pageable);
    }

    /** Lista consultas del medico autenticado. */
    @GetMapping("/doctor")
    @RequierePermiso("historial.ver_propios")
    public Page<ConsultaResponse> listarPorDoctorAutenticado(Pageable pageable) {
        return historialService.listarPorDoctorAutenticado(pageable);
    }

    /** Agrega una nota de evolucion. */
    @PostMapping("/{id}/notas")
    @RequierePermiso("historial.editar")
    public ConsultaResponse agregarNotaEvolucion(@PathVariable Long id,
                                                  @Valid @RequestBody NotaEvolucionRequest solicitud) {
        return historialService.agregarNotaEvolucion(id, solicitud);
    }

    /** Agrega un archivo adjunto a una consulta existente. */
    @PostMapping(value = "/{id}/adjuntos", consumes = "multipart/form-data")
    @RequierePermiso("historial.editar")
    public ResponseEntity<AdjuntoResponse> agregarAdjunto(@PathVariable Long id,
                                                          @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historialService.agregarAdjunto(id, archivo));
    }
}
