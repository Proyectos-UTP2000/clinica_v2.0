package com.web.clinica.repository;

import com.web.clinica.model.Permiso;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PermisoRepository extends JpaRepository<Permiso, Long> {

    /** Busca un permiso por codigo estable. */
    Optional<Permiso> findByCodigo(String codigo);

    /** Lista permisos asignados a un rol especifico. */
    List<Permiso> findByRolesNombre(String rolNombre);
}
