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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import com.web.clinica.audit.AuditAction;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;

@RestController
@RequestMapping("/api/consultas")
@RequiredArgsConstructor
public class ConsultaController {

    private final IHistorialService historialService;

    /** Crea una consulta clinica. */
    @PostMapping
    @RequierePermiso("historial.crear")
    @AuditAction("Crear consulta médica")
    public ResponseEntity<ConsultaResponse> crearConsulta(@Valid @RequestBody ConsultaCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historialService.crearConsulta(solicitud));
    }

    /** Obtiene una consulta con detalles. */
    @GetMapping("/{id}")
    @RequierePermiso({"historial.ver_todos", "historial.ver_propios", "historial.ver_basico"})
    @AuditAction("Ver detalle de consulta médica")
    public ConsultaResponse obtenerConsulta(@PathVariable Long id) {
        return historialService.obtenerConsulta(id);
    }

    /** Lista consultas de un paciente con filtros. */
    @GetMapping("/paciente/{pacienteId}")
    @RequierePermiso({"historial.ver_todos", "historial.ver_basico", "historial.ver_propios"})
    @AuditAction("Ver historial clínico por paciente")
    public Page<ConsultaResponse> listarPorPaciente(
            @PathVariable Long pacienteId,
            @RequestParam(required = false) String search,
            @RequestParam(defaultValue = "false") boolean tieneRecetas,
            @RequestParam(defaultValue = "false") boolean tieneEstudios,
            @RequestParam(defaultValue = "false") boolean tieneAdjuntos,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaInicio,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fechaFin,
            Pageable pageable) {
        return historialService.listarPorPaciente(pacienteId, search, tieneRecetas, tieneEstudios, tieneAdjuntos, fechaInicio, fechaFin, pageable);
    }

    /** Lista consultas del medico autenticado. */
    @GetMapping("/doctor")
    @RequierePermiso("historial.ver_propios")
    @AuditAction("Ver mis consultas médicas como doctor")
    public Page<ConsultaResponse> listarPorDoctorAutenticado(Pageable pageable) {
        return historialService.listarPorDoctorAutenticado(pageable);
    }

    /** Agrega una nota de evolucion. */
    @PostMapping("/{id}/notas")
    @RequierePermiso("historial.editar")
    @AuditAction("Agregar nota de evolución")
    public ConsultaResponse agregarNotaEvolucion(@PathVariable Long id,
                                                  @Valid @RequestBody NotaEvolucionRequest solicitud) {
        return historialService.agregarNotaEvolucion(id, solicitud);
    }

    /** Agrega un archivo adjunto a una consulta existente. */
    @PostMapping(value = "/{id}/adjuntos", consumes = "multipart/form-data")
    @RequierePermiso("historial.editar")
    @AuditAction("Agregar adjunto a consulta")
    public ResponseEntity<AdjuntoResponse> agregarAdjunto(@PathVariable Long id,
                                                          @RequestPart("archivo") MultipartFile archivo) {
        return ResponseEntity.status(HttpStatus.CREATED).body(historialService.agregarAdjunto(id, archivo));
    }

    /** Descarga el PDF de la consulta clinica. */
    @GetMapping("/{id}/pdf")
    @RequierePermiso({"historial.ver_todos", "historial.ver_propios", "historial.ver_basico"})
    @AuditAction("Descargar PDF de consulta médica")
    public ResponseEntity<byte[]> descargarPdf(@PathVariable Long id) {
        byte[] pdfBytes = historialService.generarPdfConsulta(id);
        org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        String nombreArchivo = "consulta_" + id + '.' + "pdf";
        headers.setContentDisposition(org.springframework.http.ContentDisposition.builder("attachment")
                .filename(nombreArchivo)
                .build());
        return new ResponseEntity<>(pdfBytes, headers, org.springframework.http.HttpStatus.OK);
    }
}
