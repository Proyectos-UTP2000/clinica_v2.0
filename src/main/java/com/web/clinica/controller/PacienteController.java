package com.web.clinica.controller;

import com.web.clinica.dto.request.PacienteCreateRequest;
import com.web.clinica.dto.request.PacienteUpdateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.PacienteResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IPacienteService;
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
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PacienteController {

    private final IPacienteService pacienteService;

    /** Lista pacientes activos con paginacion. */
    @GetMapping
    @RequierePermiso({"pacientes.ver", "usuarios.ver"})
    public Page<PacienteResponse> listarActivos(Pageable pageable) {
        return pacienteService.listarActivos(pageable);
    }

    /** Obtiene un paciente por id. */
    @GetMapping("/{id}")
    @RequierePermiso({"pacientes.ver", "usuarios.ver"})
    public PacienteResponse obtenerPorId(@PathVariable Long id) {
        return pacienteService.obtenerPorId(id);
    }

    /** Crea un nuevo paciente. */
    @PostMapping
    @RequierePermiso({"pacientes.crear", "usuarios.crear"})
    public ResponseEntity<PacienteResponse> crear(@Valid @RequestBody PacienteCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(pacienteService.crear(solicitud));
    }

    /** Actualiza un paciente existente. */
    @PutMapping("/{id}")
    @RequierePermiso({"pacientes.editar", "usuarios.editar"})
    public PacienteResponse actualizar(@PathVariable Long id, @Valid @RequestBody PacienteUpdateRequest solicitud) {
        return pacienteService.actualizar(id, solicitud);
    }

    /** Desactiva un paciente. */
    @DeleteMapping("/{id}")
    @RequierePermiso({"pacientes.eliminar", "usuarios.desactivar"})
    public ApiResponse desactivar(@PathVariable Long id) {
        pacienteService.desactivar(id);
        return new ApiResponse("Paciente desactivado correctamente", true);
    }
}
