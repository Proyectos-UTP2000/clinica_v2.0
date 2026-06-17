package com.web.clinica.controller;

import com.web.clinica.dto.request.AsignarPermisosRequest;
import com.web.clinica.dto.request.RolCreateRequest;
import com.web.clinica.dto.request.RolUpdateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.RolResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IRolService;
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
@RequestMapping("/api/roles")
@RequiredArgsConstructor
public class RolController {

    private final IRolService rolService;

    /** Lista roles para administracion. */
    @GetMapping
    @RequierePermiso("roles.ver")
    public Page<RolResponse> listar(Pageable pageable) {
        return rolService.listar(pageable);
    }

    /** Obtiene detalle de rol por id. */
    @GetMapping("/{id}")
    @RequierePermiso("roles.ver")
    public RolResponse obtenerPorId(@PathVariable Long id) {
        return rolService.obtenerPorId(id);
    }

    /** Crea un rol con permisos asignados. */
    @PostMapping
    @RequierePermiso("roles.crear")
    public ResponseEntity<RolResponse> crear(@Valid @RequestBody RolCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rolService.crear(solicitud));
    }

    /** Actualiza datos y permisos de un rol. */
    @PutMapping("/{id}")
    @RequierePermiso("roles.editar")
    public RolResponse actualizar(@PathVariable Long id, @Valid @RequestBody RolUpdateRequest solicitud) {
        return rolService.actualizar(id, solicitud);
    }

    /** Reemplaza permisos asignados a un rol. */
    @PutMapping("/{id}/permisos")
    @RequierePermiso("roles.editar")
    public RolResponse asignarPermisos(@PathVariable Long id, @Valid @RequestBody AsignarPermisosRequest solicitud) {
        return rolService.asignarPermisos(id, solicitud);
    }

    /** Desactiva un rol sin borrado fisico. */
    @DeleteMapping("/{id}")
    @RequierePermiso("roles.eliminar")
    public ApiResponse desactivar(@PathVariable Long id) {
        rolService.desactivar(id);
        return new ApiResponse("Rol desactivado correctamente", true);
    }
}
