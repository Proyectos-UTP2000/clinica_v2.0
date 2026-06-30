package com.web.clinica.repository;

import com.web.clinica.model.CajaDiaria;
import java.time.LocalDate;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CajaDiariaRepository extends JpaRepository<CajaDiaria, Long> {

    Optional<CajaDiaria> findByFecha(LocalDate fecha);

    Optional<CajaDiaria> findByFechaAndEstado(LocalDate fecha, String estado);
}
