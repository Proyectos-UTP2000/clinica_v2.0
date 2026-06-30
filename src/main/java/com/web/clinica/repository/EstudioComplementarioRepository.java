package com.web.clinica.repository;

import com.web.clinica.model.EstudioComplementario;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EstudioComplementarioRepository extends JpaRepository<EstudioComplementario, Long> {

    /** Lista estudios por consulta. */
    List<EstudioComplementario> findByConsultaId(Long consultaId);

    /** Lista estudios por estado con paginacion. */
    org.springframework.data.domain.Page<EstudioComplementario> findByEstado(String estado, org.springframework.data.domain.Pageable pageable);

    /** Busca estudios filtrando por nombres, apellidos o DNI del paciente y estado (opcional). */
    @org.springframework.data.jpa.repository.Query("SELECT e FROM EstudioComplementario e WHERE " +
            "(:estado IS NULL OR e.estado = :estado) AND " +
            "(LOWER(e.consulta.paciente.nombres) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "LOWER(e.consulta.paciente.apellidos) LIKE LOWER(CONCAT('%', :filtro, '%')) OR " +
            "e.consulta.paciente.dni LIKE CONCAT('%', :filtro, '%'))")
    org.springframework.data.domain.Page<EstudioComplementario> buscarPorEstadoYPaciente(
            @org.springframework.data.repository.query.Param("estado") String estado,
            @org.springframework.data.repository.query.Param("filtro") String filtro,
            org.springframework.data.domain.Pageable pageable);
}

