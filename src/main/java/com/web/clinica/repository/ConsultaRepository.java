package com.web.clinica.repository;

import com.web.clinica.model.Consulta;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ConsultaRepository extends JpaRepository<Consulta, Long> {

    /** Lista consultas de un paciente. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede", "cita"})
    Page<Consulta> findByPacienteId(Long pacienteId, Pageable pageable);

    /** Lista consultas de un paciente con filtros de búsqueda y presencia de recetas, estudios o adjuntos. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede", "cita"})
    @org.springframework.data.jpa.repository.Query("""
        SELECT DISTINCT c FROM Consulta c
        WHERE c.paciente.id = :pacienteId
          AND (:search IS NULL OR :search = ''
               OR LOWER(c.motivoConsulta) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.diagnostico) LIKE LOWER(CONCAT('%', :search, '%'))
               OR LOWER(c.observaciones) LIKE LOWER(CONCAT('%', :search, '%'))
               OR EXISTS (SELECT r FROM Receta r WHERE r.consulta = c AND LOWER(r.medicamento) LIKE LOWER(CONCAT('%', :search, '%')))
               OR EXISTS (SELECT e FROM EstudioComplementario e WHERE e.consulta = c AND LOWER(e.tipoEstudio) LIKE LOWER(CONCAT('%', :search, '%'))))
          AND (:tieneRecetas = false OR EXISTS (SELECT r FROM Receta r WHERE r.consulta = c))
          AND (:tieneEstudios = false OR EXISTS (SELECT e FROM EstudioComplementario e WHERE e.consulta = c))
          AND (:tieneAdjuntos = false OR EXISTS (SELECT a FROM Adjunto a WHERE a.consulta = c))
          AND (:fechaInicio IS NULL OR c.fechaHora >= :fechaInicio)
          AND (:fechaFin IS NULL OR c.fechaHora <= :fechaFin)
    """)
    Page<Consulta> findByPacienteIdWithFilters(
            @org.springframework.data.repository.query.Param("pacienteId") Long pacienteId,
            @org.springframework.data.repository.query.Param("search") String search,
            @org.springframework.data.repository.query.Param("tieneRecetas") boolean tieneRecetas,
            @org.springframework.data.repository.query.Param("tieneEstudios") boolean tieneEstudios,
            @org.springframework.data.repository.query.Param("tieneAdjuntos") boolean tieneAdjuntos,
            @org.springframework.data.repository.query.Param("fechaInicio") java.time.LocalDateTime fechaInicio,
            @org.springframework.data.repository.query.Param("fechaFin") java.time.LocalDateTime fechaFin,
            Pageable pageable);

    /** Lista consultas asociadas al usuario medico. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede", "cita"})
    Page<Consulta> findByDoctorUsuarioId(Long usuarioId, Pageable pageable);

    /** Lista consultas de un paciente filtradas por estado. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede", "cita"})
    Page<Consulta> findByPacienteIdAndEstado(Long pacienteId, String estado, Pageable pageable);
}
