package com.web.clinica.repository;

import com.web.clinica.model.DisponibilidadBase;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Sede;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DisponibilidadBaseRepository extends JpaRepository<DisponibilidadBase, Long> {

    /** Busca horarios base por doctor, sede y dia. */
    List<DisponibilidadBase> findByDoctorAndSedeAndDiaSemana(Doctor doctor, Sede sede, int diaSemana);
}
