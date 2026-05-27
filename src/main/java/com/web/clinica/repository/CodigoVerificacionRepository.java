package com.web.clinica.repository;

import com.web.clinica.model.CodigoVerificacion;
import java.time.LocalDateTime;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CodigoVerificacionRepository extends JpaRepository<CodigoVerificacion, Long> {

    /** Busca un codigo vigente y no usado para restablecer password. */
    Optional<CodigoVerificacion> findByEmailAndCodigoAndUsadoFalseAndFechaExpiracionAfter(
            String email,
            String codigo,
            LocalDateTime ahora
    );
}
