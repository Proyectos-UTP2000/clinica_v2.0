package com.web.clinica.controller;

import com.web.clinica.dto.request.CajaDiariaRequest;
import com.web.clinica.dto.request.CierreCajaRequest;
import com.web.clinica.dto.response.CajaDiariaResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.ICajaDiariaService;
import jakarta.validation.Valid;
import java.time.LocalDate;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/caja")
@RequiredArgsConstructor
public class CajaDiariaController {

    private final ICajaDiariaService cajaDiariaService;

    @PostMapping("/abrir")
    @RequierePermiso("caja.gestionar")
    public ResponseEntity<CajaDiariaResponse> abrirCaja(@Valid @RequestBody CajaDiariaRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(cajaDiariaService.abrirCaja(solicitud));
    }

    @PostMapping("/cerrar")
    @RequierePermiso("caja.gestionar")
    public ResponseEntity<CajaDiariaResponse> cerrarCaja(@Valid @RequestBody CierreCajaRequest solicitud) {
        return ResponseEntity.ok(cajaDiariaService.cerrarCaja(solicitud));
    }

    @GetMapping("/hoy")
    @RequierePermiso("caja.ver")
    public ResponseEntity<CajaDiariaResponse> obtenerCajaDelDia() {
        return ResponseEntity.ok(cajaDiariaService.obtenerCajaDelDia());
    }

    @GetMapping("/fecha/{fecha}")
    @RequierePermiso("caja.ver")
    public ResponseEntity<CajaDiariaResponse> obtenerCajaPorFecha(@PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fecha) {
        return ResponseEntity.ok(cajaDiariaService.obtenerCajaPorFecha(fecha));
    }

    @GetMapping("/{id}/reporte-pdf")
    @RequierePermiso("caja.ver")
    public ResponseEntity<byte[]> descargarReportePdf(@PathVariable Long id) {
        byte[] pdfBytes = cajaDiariaService.generarReportePdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(org.springframework.http.MediaType.APPLICATION_PDF);
        String nombreArchivo = "reporte_caja_" + id + '.' + "pdf";
        headers.setContentDispositionFormData("attachment", nombreArchivo);
        return new ResponseEntity<>(pdfBytes, headers, HttpStatus.OK);
    }

    @PostMapping("/reabrir")
    @RequierePermiso("caja.gestionar")
    public ResponseEntity<CajaDiariaResponse> reabrirCaja() {
        return ResponseEntity.ok(cajaDiariaService.reabrirCaja());
    }
}
