package com.web.clinica.repository;

import com.web.clinica.model.Receta;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RecetaRepository extends JpaRepository<Receta, Long> {

    /** Lista recetas por consulta. */
    List<Receta> findByConsultaId(Long consultaId);
}
