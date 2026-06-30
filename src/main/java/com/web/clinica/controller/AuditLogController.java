package com.web.clinica.controller;

import com.web.clinica.model.AuditLog;
import com.web.clinica.repository.AuditLogRepository;
import com.web.clinica.security.RequierePermiso;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/audit-logs")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogRepository auditLogRepository;

    @GetMapping
    @RequierePermiso("audit.ver")
    public ResponseEntity<Page<AuditLog>> listarAuditLogs(
            @RequestParam(required = false, defaultValue = "") String search,
            Pageable pageable) {
        Page<AuditLog> logs = auditLogRepository.findByUsuarioDniContainingOrAccionContainingOrDetallesContainingOrderByFechaDesc(
                search, search, search, pageable
        );
        return ResponseEntity.ok(logs);
    }
}
