package com.web.clinica.security;

import com.web.clinica.exception.AccesoDenegadoException;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Aspect
@Component
@RequiredArgsConstructor
public class PermisoAspect {

    /** Verifica que el usuario autenticado tenga el permiso requerido. */
    @Around("@annotation(requierePermiso)")
    public Object verificarPermiso(ProceedingJoinPoint punto, RequierePermiso requierePermiso) throws Throwable {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new AccesoDenegadoException("Debe autenticarse para acceder a este recurso");
        }

        boolean tienePermiso = autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> Objects.equals(autoridad.getAuthority(), requierePermiso.value()));

        if (!tienePermiso) {
            throw new AccesoDenegadoException("No cuenta con el permiso requerido: " + requierePermiso.value());
        }

        return punto.proceed();
    }
}
