package com.web.clinica.controller;

import com.web.clinica.dto.response.AdjuntoDownloadResponse;
import com.web.clinica.dto.response.EstudioResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IHistorialService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/estudios")
@RequiredArgsConstructor
public class EstudioController {

    private final IHistorialService historialService;

    /** Lista estudios complementarios con filtros de estado y busqueda de paciente. */
    @GetMapping
    @RequierePermiso({"historial.ver_todos", "historial.ver_propios", "historial.ver_basico"})
    public Page<EstudioResponse> listarEstudios(
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String filtro,
            Pageable pageable) {
        return historialService.listarEstudios(estado, filtro, pageable);
    }

    /** Sube los archivos de resultado de un estudio complementario y lo marca como completado. */
    @PostMapping(value = "/{id}/resultado", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @RequierePermiso("historial.editar")
    public ResponseEntity<EstudioResponse> registrarResultado(
            @PathVariable Long id,
            @RequestParam("archivos") java.util.List<MultipartFile> archivos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(historialService.registrarResultadoEstudio(id, archivos));
    }

    /** Descarga el archivo de resultado de un estudio complementario por índice. */
    @GetMapping("/{id}/resultado/descargar")
    @RequierePermiso({"historial.ver_todos", "historial.ver_propios", "historial.ver_basico"})
    public ResponseEntity<?> descargarResultado(
            @PathVariable Long id,
            @RequestParam(value = "index", defaultValue = "0") Integer index) {
        AdjuntoDownloadResponse descarga = historialService.descargarResultadoEstudio(id, index);
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(descarga.tipoMime()))
                .header(HttpHeaders.CONTENT_DISPOSITION, ContentDisposition.attachment()
                        .filename(descarga.nombreArchivo())
                        .build()
                        .toString())
                .body(descarga.resource());
    }
}
