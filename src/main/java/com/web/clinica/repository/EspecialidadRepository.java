package com.web.clinica.repository;

import com.web.clinica.model.Especialidad;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EspecialidadRepository extends JpaRepository<Especialidad, Long> {

    /** Lista todas las especialidades existentes. */
    List<Especialidad> findAllByOrderByNombreAsc();
}
