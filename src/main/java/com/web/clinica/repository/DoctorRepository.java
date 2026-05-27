package com.web.clinica.repository;

import com.web.clinica.model.Doctor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /** Busca un doctor por DNI del usuario asociado. */
    Optional<Doctor> findByUsuarioDni(String dni);

    /** Lista doctores cuyo usuario asociado esta activo. */
    @Query("SELECT d FROM Doctor d WHERE d.usuario.activo = true")
    List<Doctor> findByActivoTrue();

    /** Lista doctores activos con datos necesarios para DTO. */
    @EntityGraph(attributePaths = {"usuario", "especialidad", "subespecialidad", "sedes"})
    @Query("SELECT d FROM Doctor d WHERE d.usuario.activo = true")
    Page<Doctor> listarActivos(Pageable pageable);

    /** Filtra doctores activos por nombre, especialidad o sede. */
    @EntityGraph(attributePaths = {"usuario", "especialidad", "subespecialidad", "sedes"})
    @Query("""
            SELECT DISTINCT d FROM Doctor d
            LEFT JOIN d.sedes s
            WHERE d.usuario.activo = true
              AND (:texto IS NULL OR LOWER(CONCAT(d.usuario.nombres, ' ', d.usuario.apellidos)) LIKE LOWER(CONCAT('%', :texto, '%')))
              AND (:especialidadId IS NULL OR d.especialidad.id = :especialidadId)
              AND (:sedeId IS NULL OR s.id = :sedeId)
            """)
    Page<Doctor> listarConFiltros(@Param("texto") String texto,
                                  @Param("especialidadId") Long especialidadId,
                                  @Param("sedeId") Long sedeId,
                                  Pageable pageable);
}
