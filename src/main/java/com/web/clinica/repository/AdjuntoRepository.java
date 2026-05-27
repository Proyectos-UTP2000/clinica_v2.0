package com.web.clinica.repository;

import com.web.clinica.model.Adjunto;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdjuntoRepository extends JpaRepository<Adjunto, Long> {

    /** Lista adjuntos por consulta. */
    List<Adjunto> findByConsultaId(Long consultaId);
}
