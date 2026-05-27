package com.web.clinica.repository;

import com.web.clinica.model.Paciente;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PacienteRepository extends JpaRepository<Paciente, Long> {

    /** Busca pacientes por DNI unico. */
    Optional<Paciente> findByDni(String dni);

    /** Lista pacientes activos sin paginacion. */
    List<Paciente> findByActivoTrue();

    /** Lista pacientes activos con paginacion. */
    Page<Paciente> findByActivoTrue(Pageable pageable);
}
