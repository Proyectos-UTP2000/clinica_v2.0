package com.web.clinica.repository;

import com.web.clinica.model.IndicacionMedica;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface IndicacionMedicaRepository extends JpaRepository<IndicacionMedica, Long> {

    /** Lista indicaciones por consulta. */
    List<IndicacionMedica> findByConsultaId(Long consultaId);
}
