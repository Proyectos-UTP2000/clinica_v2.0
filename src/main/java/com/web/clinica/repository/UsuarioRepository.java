package com.web.clinica.repository;

import com.web.clinica.model.Usuario;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    /** Busca un usuario por DNI para autenticacion. */
    Optional<Usuario> findByDni(String dni);

    /** Busca un usuario por correo para recuperacion. */
    Optional<Usuario> findByEmail(String email);

    /** Valida que DNI y correo pertenezcan al mismo usuario. */
    Optional<Usuario> findByDniAndEmail(String dni, String email);
}
