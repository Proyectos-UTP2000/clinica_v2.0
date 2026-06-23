package com.web.clinica.controller;

import com.web.clinica.dto.request.ConsultorioCreateRequest;
import com.web.clinica.dto.request.ConsultorioUpdateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.ConsultorioResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IConsultorioService;
import jakarta.validation.Valid;
import java.util.List;
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
@RequestMapping("/api/consultorios")
@RequiredArgsConstructor
public class ConsultorioController {

    private final IConsultorioService consultorioService;

    @GetMapping
    @RequierePermiso("consultorios.ver")
    public Page<ConsultorioResponse> listarActivos(Pageable pageable) {
        return consultorioService.listarActivos(pageable);
    }

    @GetMapping("/{id}")
    @RequierePermiso("consultorios.ver")
    public ConsultorioResponse obtenerPorId(@PathVariable Long id) {
        return consultorioService.obtenerPorId(id);
    }

    @GetMapping("/sede/{sedeId}")
    @RequierePermiso({"consultorios.ver", "citas.crear", "citas.ver_todas", "citas.editar_asignados"})
    public List<ConsultorioResponse> listarPorSede(@PathVariable Long sedeId) {
        return consultorioService.listarPorSede(sedeId);
    }

    @PostMapping
    @RequierePermiso("consultorios.crear")
    public ResponseEntity<ConsultorioResponse> crear(@Valid @RequestBody ConsultorioCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(consultorioService.crear(solicitud));
    }

    @PutMapping("/{id}")
    @RequierePermiso("consultorios.editar")
    public ConsultorioResponse actualizar(@PathVariable Long id, @Valid @RequestBody ConsultorioUpdateRequest solicitud) {
        return consultorioService.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @RequierePermiso("consultorios.eliminar")
    public ApiResponse desactivar(@PathVariable Long id) {
        consultorioService.desactivar(id);
        return new ApiResponse("Consultorio desactivado correctamente", true);
    }
}
