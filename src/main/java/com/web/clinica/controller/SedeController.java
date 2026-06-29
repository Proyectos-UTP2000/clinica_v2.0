package com.web.clinica.controller;

import com.web.clinica.dto.request.SedeCreateRequest;
import com.web.clinica.dto.request.SedeUpdateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.SedeResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.ISedeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/sedes")
@RequiredArgsConstructor
public class SedeController {

    private final ISedeService sedeService;

    /** Lista sedes activas. */
    @GetMapping
    public Page<SedeResponse> listarActivos(Pageable pageable) {
        return sedeService.listarActivos(pageable);
    }

    /** Obtiene una sede por id. */
    @GetMapping("/{id}")
    @RequierePermiso("sedes.ver")
    public SedeResponse obtenerPorId(@PathVariable Long id) {
        return sedeService.obtenerPorId(id);
    }

    /** Crea una sede. */
    @PostMapping
    @RequierePermiso("sedes.crear")
    public ResponseEntity<SedeResponse> crear(@Valid @RequestBody SedeCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sedeService.crear(solicitud));
    }

    /** Actualiza una sede. */
    @PutMapping("/{id}")
    @RequierePermiso("sedes.editar")
    public SedeResponse actualizar(@PathVariable Long id, @Valid @RequestBody SedeUpdateRequest solicitud) {
        return sedeService.actualizar(id, solicitud);
    }

    /** Desactiva una sede. */
    @DeleteMapping("/{id}")
    @RequierePermiso("sedes.eliminar")
    public ApiResponse desactivar(@PathVariable Long id) {
        sedeService.desactivar(id);
        return new ApiResponse("Sede desactivada correctamente", true);
    }
}
