package com.web.clinica.controller;

import com.web.clinica.dto.response.AdjuntoDownloadResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IHistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/adjuntos")
@RequiredArgsConstructor
public class AdjuntoController {

    private final IHistorialService historialService;

    /** Descarga un adjunto del historial clinico. */
    @GetMapping("/{id}")
    @RequierePermiso({"historial.ver_todos", "historial.ver_propios", "historial.ver_basico"})
    public ResponseEntity<?> descargarAdjunto(@PathVariable Long id) {
        AdjuntoDownloadResponse descarga = historialService.descargarAdjunto(id);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(descarga.tipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(descarga.nombreArchivo())
                        .build()
                        .toString())
                .body(descarga.resource());
    }
}
