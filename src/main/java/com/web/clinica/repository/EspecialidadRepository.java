package com.web.clinica.repository;

import com.web.clinica.model.Especialidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    /** Lista todas las especialidades existentes. */
    List<Especialidad> findAllByOrderByNombreAsc();

    /** Lista especialidades con paginacion. */
    Page<Especialidad> findAllByOrderByNombreAsc(Pageable pageable);

    /** Busca especialidad por nombre exacto. */
    Optional<Especialidad> findByNombre(String nombre);

    /** Verifica nombre duplicado excluyendo la especialidad actual. */
    boolean existsByNombreAndIdNot(String nombre, Long id);
}
