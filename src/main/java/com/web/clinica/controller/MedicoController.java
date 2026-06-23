package com.web.clinica.controller;

import com.web.clinica.dto.request.MedicoCreateRequest;
import com.web.clinica.dto.request.MedicoUpdateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.MedicoResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IMedicoService;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/medicos")
@RequiredArgsConstructor
public class MedicoController {

    private final IMedicoService medicoService;

    /** Lista medicos activos con filtros opcionales. */
    @GetMapping
    @RequierePermiso({"medicos.ver", "usuarios.ver"})
    public Page<MedicoResponse> listar(@RequestParam(required = false) String texto,
                                        @RequestParam(required = false) Long especialidadId,
                                        @RequestParam(required = false) Long sedeId,
                                        Pageable pageable) {
        return medicoService.listarActivos(texto, especialidadId, sedeId, pageable);
    }

    /** Busca medicos activos por sede y especialidad para agenda/reprogramacion. */
    @GetMapping("/buscar")
    @RequierePermiso({"medicos.ver", "citas.crear", "citas.editar_propias", "citas.editar_asignados"})
    public Page<MedicoResponse> buscar(@RequestParam(required = false) Long sedeId,
                                       @RequestParam(required = false) Long especialidadId,
                                       Pageable pageable) {
        return medicoService.listarActivos(null, especialidadId, sedeId, pageable);
    }

    /** Consulta datos externos por DNI para prellenar formularios de medicos. */
    @GetMapping("/buscar-dni")
    @RequierePermiso({"medicos.crear", "medicos.editar", "usuarios.crear"})
    public MedicoResponse consultarDni(@RequestParam String dni) {
        return medicoService.consultarDni(dni);
    }

    /** Obtiene el medico autenticado para pantallas de disponibilidad propia. */
    @GetMapping("/me")
    @RequierePermiso("disponibilidad.ver_propia")
    public MedicoResponse obtenerAutenticado() {
        return medicoService.obtenerAutenticado();
    }

    /** Obtiene un medico por id. */
    @GetMapping("/{id}")
    @RequierePermiso({"medicos.ver", "usuarios.ver"})
    public MedicoResponse obtener(@PathVariable Long id) {
        return medicoService.obtenerPorId(id);
    }

    /** Crea un medico y usuario interno. */
    @PostMapping
    @RequierePermiso({"medicos.crear", "usuarios.crear"})
    public ResponseEntity<MedicoResponse> crear(@Valid @RequestBody MedicoCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(medicoService.crear(solicitud));
    }

    /** Actualiza datos del medico. */
    @PutMapping("/{id}")
    @RequierePermiso({"medicos.editar", "usuarios.editar"})
    public MedicoResponse actualizar(@PathVariable Long id, @Valid @RequestBody MedicoUpdateRequest solicitud) {
        return medicoService.actualizar(id, solicitud);
    }

    /** Desactiva el usuario asociado al medico. */
    @DeleteMapping("/{id}")
    @RequierePermiso({"medicos.eliminar", "usuarios.desactivar"})
    public ApiResponse desactivar(@PathVariable Long id) {
        medicoService.desactivar(id);
        return new ApiResponse("Medico desactivado correctamente", true);
    }
}
