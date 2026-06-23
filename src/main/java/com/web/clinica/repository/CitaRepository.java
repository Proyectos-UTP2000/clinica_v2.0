package com.web.clinica.repository;

import com.web.clinica.model.Cita;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Paciente;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CitaRepository extends JpaRepository<Cita, Long>, JpaSpecificationExecutor<Cita> {

    /** Cuenta citas programadas dentro de un rango. */
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.fechaHoraInicio BETWEEN :inicio AND :fin")
    long countCitasProgramadasEntre(@Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin);

    /** Cuenta citas por estados para dashboard. */
    @Query("SELECT COUNT(c) FROM Cita c WHERE c.estado IN :estados")
    long countByEstadoIn(@Param("estados") List<String> estados);

    /** Devuelve ranking de medicos por citas atendidas en un rango. */
    @Query("""
            SELECT c.doctor, COUNT(c) FROM Cita c
            WHERE c.estado = 'atendida' AND c.fechaHoraInicio BETWEEN :inicio AND :fin
            GROUP BY c.doctor
            ORDER BY COUNT(c) DESC
            """)
    List<Object[]> rankingMedicosPorCitasAtendidas(@Param("inicio") LocalDateTime inicio,
                                                   @Param("fin") LocalDateTime fin);

    /** Busca citas del doctor en un rango horario. */
    List<Cita> findByDoctorAndFechaHoraInicioBetween(Doctor doctor, LocalDateTime inicio, LocalDateTime fin);

    /** Busca citas del paciente en un rango horario. */
    List<Cita> findByPacienteAndFechaHoraInicioBetween(Paciente paciente, LocalDateTime inicio, LocalDateTime fin);

    /** Busca cita no eliminada logicamente por estado. */
    Optional<Cita> findByIdAndEstadoNot(Long id, String estado);

    /** Lista citas por paciente. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    Page<Cita> findByPacienteId(Long pacienteId, Pageable pageable);

    /** Lista citas por doctor. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    Page<Cita> findByDoctorId(Long doctorId, Pageable pageable);

    /** Lista todas las citas con datos necesarios para DTO. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    Page<Cita> findAll(Pageable pageable);

    /** Lista citas por fecha. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    Page<Cita> findByFechaHoraInicioBetween(LocalDateTime inicio, LocalDateTime fin, Pageable pageable);

    /** Lista citas por paciente y doctor. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    Page<Cita> findByPacienteIdAndDoctorId(Long pacienteId, Long doctorId, Pageable pageable);

    /** Lista citas por doctor y rango de fecha. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    Page<Cita> findByDoctorIdAndFechaHoraInicioBetween(Long doctorId, LocalDateTime inicio, LocalDateTime fin, Pageable pageable);

    /** Lista citas por paciente y rango de fecha. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    Page<Cita> findByPacienteIdAndFechaHoraInicioBetween(Long pacienteId, LocalDateTime inicio, LocalDateTime fin, Pageable pageable);

    /** Lista citas por paciente, doctor y rango de fecha. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    Page<Cita> findByPacienteIdAndDoctorIdAndFechaHoraInicioBetween(Long pacienteId,
                                                                    Long doctorId,
                                                                    LocalDateTime inicio,
                                                                    LocalDateTime fin,
                                                                    Pageable pageable);

    /** Lista citas aplicando filtros opcionales. */
    @EntityGraph(attributePaths = {"paciente", "doctor", "doctor.usuario", "sede"})
    @Query("""
            SELECT c FROM Cita c
            WHERE (:pacienteId IS NULL OR c.paciente.id = :pacienteId)
              AND (:doctorId IS NULL OR c.doctor.id = :doctorId)
              AND (:inicio IS NULL OR c.fechaHoraInicio >= :inicio)
              AND (:fin IS NULL OR c.fechaHoraInicio < :fin)
            """)
    Page<Cita> listarConFiltros(@Param("pacienteId") Long pacienteId,
                                @Param("doctorId") Long doctorId,
                                @Param("inicio") LocalDateTime inicio,
                                @Param("fin") LocalDateTime fin,
                                Pageable pageable);

    @Query("SELECT COUNT(c) FROM Cita c WHERE c.consultorio.id = :consultorioId AND c.estado <> 'cancelada' AND c.fechaHoraInicio < :fin AND c.fechaHoraFin > :inicio AND (:excludeCitaId IS NULL OR c.id <> :excludeCitaId)")
    long countCitasSolapadasEnConsultorio(@Param("consultorioId") Long consultorioId, @Param("inicio") LocalDateTime inicio, @Param("fin") LocalDateTime fin, @Param("excludeCitaId") Long excludeCitaId);

    @Query("SELECT c FROM Cita c WHERE c.fechaHoraFin < :ahora AND c.estado IN :estados")
    List<Cita> buscarCitasVencidas(@Param("ahora") LocalDateTime ahora, @Param("estados") List<String> estados);
}
