package com.web.clinica.security;

import com.web.clinica.exception.AccesoDenegadoException;
import java.util.Arrays;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
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

    private static final String ROL_ADMINISTRADOR = "ROLE_Administrador";

    /** Verifica que el usuario autenticado tenga el permiso requerido. */
    @Around("@annotation(requierePermiso)")
    public Object verificarPermiso(ProceedingJoinPoint punto, RequierePermiso requierePermiso) throws Throwable {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null || !autenticacion.isAuthenticated()) {
            throw new AccesoDenegadoException("Debe autenticarse para acceder a este recurso");
        }

        Set<String> permisosRequeridos = Arrays.stream(requierePermiso.value()).collect(Collectors.toSet());
        boolean tienePermiso = autenticacion.getAuthorities().stream()
                .anyMatch(autoridad -> Objects.equals(autoridad.getAuthority(), ROL_ADMINISTRADOR)
                        || permisosRequeridos.stream()
                        .anyMatch(permiso -> Objects.equals(autoridad.getAuthority(), permiso)));

        if (!tienePermiso) {
            throw new AccesoDenegadoException("No cuenta con alguno de los permisos requeridos: " + permisosRequeridos);
        }

        return punto.proceed();
    }
}
