package com.web.clinica.repository;

import com.web.clinica.model.DisponibilidadBase;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Sede;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisponibilidadBaseRepository extends JpaRepository<DisponibilidadBase, Long> {

    /** Busca horarios base por doctor, sede y dia. */
    List<DisponibilidadBase> findByDoctorAndSedeAndDiaSemana(Doctor doctor, Sede sede, int diaSemana);

    /** Lista horarios base de un doctor ordenados para la vista semanal. */
    List<DisponibilidadBase> findByDoctorIdOrderByDiaSemanaAscHoraInicioAsc(Long doctorId);

    /** Busca un horario base dentro del alcance de un doctor. */
    java.util.Optional<DisponibilidadBase> findByIdAndDoctorId(Long id, Long doctorId);
}
