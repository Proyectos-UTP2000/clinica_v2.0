package com.web.clinica.repository;

import com.web.clinica.model.Secretaria;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SecretariaRepository extends JpaRepository<Secretaria, Long> {

    /** Busca secretaria por usuario autenticado. */
    Optional<Secretaria> findByUsuarioId(Long usuarioId);

    /** Verifica si una secretaria tiene asignado un doctor. */
    boolean existsByUsuarioIdAndDoctoresId(Long usuarioId, Long doctorId);
}
