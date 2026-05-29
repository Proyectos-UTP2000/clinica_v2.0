package com.web.clinica.repository;

import com.web.clinica.model.Doctor;
import com.web.clinica.model.ExcepcionDisponibilidad;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ExcepcionDisponibilidadRepository extends JpaRepository<ExcepcionDisponibilidad, Long> {

    /** Busca excepciones de agenda del doctor en una fecha. */
    List<ExcepcionDisponibilidad> findByDoctorAndFecha(Doctor doctor, LocalDate fecha);

    /** Lista excepciones en un rango de fechas para calendario operativo. */
    List<ExcepcionDisponibilidad> findByDoctorIdAndFechaBetweenOrderByFechaAscHoraInicioAsc(Long doctorId,
                                                                                            LocalDate fechaInicio,
                                                                                            LocalDate fechaFin);

    /** Busca una excepcion dentro del alcance de un doctor. */
    java.util.Optional<ExcepcionDisponibilidad> findByIdAndDoctorId(Long id, Long doctorId);
}
