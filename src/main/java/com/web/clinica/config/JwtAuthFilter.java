package com.web.clinica.config;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtProvider jwtProvider;
    private final UserDetailsService userDetailsService;

    /** Lee el Bearer token y carga la autenticacion con permisos. */
    @Override
    protected void doFilterInternal(
            HttpServletRequest solicitud,
            HttpServletResponse respuesta,
            FilterChain cadena
    ) throws ServletException, IOException {
        String token = obtenerToken(solicitud);
        if (token == null) {
            cadena.doFilter(solicitud, respuesta);
            return;
        }

        try {
            jwtProvider.validarToken(token);
            String dni = jwtProvider.obtenerDni(token);
            UserDetails usuario = userDetailsService.loadUserByUsername(dni);
            UsernamePasswordAuthenticationToken autenticacion = new UsernamePasswordAuthenticationToken(
                    usuario,
                    null,
                    usuario.getAuthorities()
            );
            autenticacion.setDetails(new WebAuthenticationDetailsSource().buildDetails(solicitud));
            SecurityContextHolder.getContext().setAuthentication(autenticacion);
            cadena.doFilter(solicitud, respuesta);
        } catch (RuntimeException excepcion) {
            SecurityContextHolder.clearContext();
            respuesta.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Token JWT invalido o expirado");
        }
    }

    /** Extrae el token Bearer del header Authorization. */
    private String obtenerToken(HttpServletRequest solicitud) {
        String cabecera = solicitud.getHeader(HttpHeaders.AUTHORIZATION);
        if (cabecera == null || !cabecera.startsWith("Bearer ")) {
            return null;
        }
        return cabecera.substring(7);
    }
}
