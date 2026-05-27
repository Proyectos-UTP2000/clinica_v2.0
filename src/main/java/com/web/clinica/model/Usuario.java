package com.web.clinica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.JoinTable;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.Getter;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

@Getter
@Setter
@Entity
@Table(name = "usuario")
public class Usuario implements UserDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String dni;

    @Column(nullable = false, length = 100)
    private String nombres;

    @Column(nullable = false, length = 100)
    private String apellidos;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(length = 20)
    private String telefono;

    @Column(name = "fecha_nacimiento")
    private LocalDate fechaNacimiento;

    @Column(name = "password_hash", nullable = false)
    private String passwordHash;

    @Column(name = "cambio_password_obligatorio", nullable = false)
    private Boolean cambioPasswordObligatorio = true;

    @Column(nullable = false)
    private Boolean activo = true;

    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "usuario_rol",
            joinColumns = @JoinColumn(name = "usuario_id"),
            inverseJoinColumns = @JoinColumn(name = "rol_id")
    )
    private Set<Rol> roles = new HashSet<>();

    /** Devuelve roles y permisos como authorities para Spring Security. */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<SimpleGrantedAuthority> autoridades = new HashSet<>();
        roles.forEach(rol -> {
            autoridades.add(new SimpleGrantedAuthority("ROLE_" + rol.getNombre()));
            autoridades.addAll(rol.getPermisos().stream()
                    .map(Permiso::getCodigo)
                    .map(SimpleGrantedAuthority::new)
                    .collect(Collectors.toSet()));
        });
        return autoridades;
    }

    /** Devuelve el hash BCrypt guardado en la base de datos. */
    @Override
    public String getPassword() {
        return passwordHash;
    }

    /** Usa el DNI como nombre de usuario para iniciar sesion. */
    @Override
    public String getUsername() {
        return dni;
    }

    /** Mantiene la cuenta sin expiracion propia. */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /** Mantiene la cuenta sin bloqueo propio. */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /** Mantiene las credenciales sin expiracion propia. */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /** Habilita el usuario solo si esta activo. */
    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(activo);
    }
}
