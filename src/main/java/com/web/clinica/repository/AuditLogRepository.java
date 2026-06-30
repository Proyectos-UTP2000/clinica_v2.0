package com.web.clinica.repository;

import com.web.clinica.model.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByUsuarioDniContainingOrAccionContainingOrDetallesContainingOrderByFechaDesc(
            String dni, String accion, String detalles, Pageable pageable);
}
