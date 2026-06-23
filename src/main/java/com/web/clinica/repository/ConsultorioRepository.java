package com.web.clinica.repository;

import com.web.clinica.model.Consultorio;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ConsultorioRepository extends JpaRepository<Consultorio, Long> {
    List<Consultorio> findBySedeIdAndActivoTrue(Long sedeId);
    Page<Consultorio> findByActivoTrue(Pageable pageable);
    Optional<Consultorio> findBySedeIdAndNombreIgnoreCase(Long sedeId, String nombre);
}
