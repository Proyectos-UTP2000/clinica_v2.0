package com.web.clinica.repository;

import com.web.clinica.model.Sede;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SedeRepository extends JpaRepository<Sede, Long> {

    /** Lista sedes activas. */
    List<Sede> findByActivoTrue();

    /** Lista sedes activas con paginacion. */
    Page<Sede> findByActivoTrue(Pageable pageable);
}
