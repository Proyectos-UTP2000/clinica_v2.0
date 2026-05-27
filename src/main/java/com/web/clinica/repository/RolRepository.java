package com.web.clinica.repository;

import com.web.clinica.model.Rol;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RolRepository extends JpaRepository<Rol, Long> {

    /** Busca un rol por nombre visible. */
    Optional<Rol> findByNombre(String nombre);
}
