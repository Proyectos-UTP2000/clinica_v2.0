package com.web.clinica.controller;

import com.web.clinica.dto.request.EspecialidadCreateRequest;
import com.web.clinica.dto.request.EspecialidadUpdateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.EspecialidadResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IEspecialidadService;
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
@RequestMapping("/api/especialidades")
@RequiredArgsConstructor
public class EspecialidadController {

    private final IEspecialidadService especialidadService;

    /** Lista especialidades con paginacion. */
    @GetMapping
    public Page<EspecialidadResponse> listar(Pageable pageable) {
        return especialidadService.listar(pageable);
    }

    /** Lista todas las especialidades para combos. */
    @GetMapping("/todas")
    public List<EspecialidadResponse> listarTodas() {
        return especialidadService.listarTodas();
    }

    /** Obtiene una especialidad por id. */
    @GetMapping("/{id}")
    @RequierePermiso("especialidades.ver")
    public EspecialidadResponse obtenerPorId(@PathVariable Long id) {
        return especialidadService.obtenerPorId(id);
    }

    /** Crea una especialidad. */
    @PostMapping
    @RequierePermiso("especialidades.crear")
    public ResponseEntity<EspecialidadResponse> crear(@Valid @RequestBody EspecialidadCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(especialidadService.crear(solicitud));
    }

    /** Actualiza una especialidad. */
    @PutMapping("/{id}")
    @RequierePermiso("especialidades.editar")
    public EspecialidadResponse actualizar(@PathVariable Long id,
                                           @Valid @RequestBody EspecialidadUpdateRequest solicitud) {
        return especialidadService.actualizar(id, solicitud);
    }

    /** Elimina una especialidad. */
    @DeleteMapping("/{id}")
    @RequierePermiso("especialidades.eliminar")
    public ApiResponse eliminar(@PathVariable Long id) {
        especialidadService.eliminar(id);
        return new ApiResponse("Especialidad eliminada correctamente", true);
    }
}
