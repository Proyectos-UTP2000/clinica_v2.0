package com.web.clinica.repository;

import com.web.clinica.model.EstudioComplementario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudioComplementarioRepository extends JpaRepository<EstudioComplementario, Long> {

    /** Lista estudios por consulta. */
    List<EstudioComplementario> findByConsultaId(Long consultaId);
}
