package com.web.clinica.controller;

import com.web.clinica.dto.response.PermisoResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IPermisoService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/permisos")
@RequiredArgsConstructor
public class PermisoController {

    private final IPermisoService permisoService;

    /** Lista permisos disponibles para asignacion a roles. */
    @GetMapping
    @RequierePermiso("roles.ver")
    public List<PermisoResponse> listar() {
        return permisoService.listar();
    }
}
