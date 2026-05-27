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

    /** Lista consultas asociadas al usuario medico. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede", "cita"})
    Page<Consulta> findByDoctorUsuarioId(Long usuarioId, Pageable pageable);

    /** Lista consultas de un paciente filtradas por estado. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede", "cita"})
    Page<Consulta> findByPacienteIdAndEstado(Long pacienteId, String estado, Pageable pageable);
}
