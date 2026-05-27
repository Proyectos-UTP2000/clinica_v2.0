package com.web.clinica.repository;

import com.web.clinica.model.Sede;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SedeRepository extends JpaRepository<Sede, Long> {

    /** Lista sedes activas. */
    List<Sede> findByActivoTrue();
}
