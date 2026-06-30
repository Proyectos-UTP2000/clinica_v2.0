package com.web.clinica.repository;

import com.web.clinica.model.Pago;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PagoRepository extends JpaRepository<Pago, Long> {

    /** Busca pago por cita. */
    @EntityGraph(attributePaths = {"cita", "registradoPorUsuario"})
    Optional<Pago> findByCitaId(Long citaId);

    /** Lista pagos de un paciente. */
    @EntityGraph(attributePaths = {"cita", "registradoPorUsuario"})
    List<Pago> findByCitaPacienteId(Long pacienteId);

    /** Lista pagos vinculados a una caja diaria. */
    @EntityGraph(attributePaths = {"cita", "cita.paciente", "registradoPorUsuario"})
    List<Pago> findByCajaDiariaId(Long cajaDiariaId);
}
