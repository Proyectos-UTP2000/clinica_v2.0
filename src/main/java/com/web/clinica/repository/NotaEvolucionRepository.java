package com.web.clinica.repository;

import com.web.clinica.model.NotaEvolucion;
import java.util.List;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotaEvolucionRepository extends JpaRepository<NotaEvolucion, Long> {

    /** Lista notas por consulta con autor cargado. */
    @EntityGraph(attributePaths = {"autor"})
    List<NotaEvolucion> findByConsultaId(Long consultaId);
}
