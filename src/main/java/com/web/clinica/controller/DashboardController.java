package com.web.clinica.controller;

import com.web.clinica.dto.response.DashboardTotalesResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IDashboardService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final IDashboardService dashboardService;

    /** Devuelve totales principales del dashboard. */
    @GetMapping("/totales")
    @RequierePermiso("dashboard.ver")
    public DashboardTotalesResponse obtenerTotales() {
        return dashboardService.obtenerTotales();
    }
}
