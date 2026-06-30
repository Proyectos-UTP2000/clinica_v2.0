package com.web.clinica.controller;

import com.web.clinica.dto.request.CambioPasswordRequest;
import com.web.clinica.dto.request.LoginRequest;
import com.web.clinica.dto.request.MiPerfilRequest;
import com.web.clinica.dto.request.RecuperarPasswordRequest;
import com.web.clinica.dto.request.ValidarCodigoRequest;
import com.web.clinica.dto.request.VerificarCodigoRecuperacionRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.JwtResponse;
import com.web.clinica.dto.response.UsuarioResponse;
import com.web.clinica.model.Usuario;
import com.web.clinica.service.abstractService.IAuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final IAuthService authService;

    /** Autentica al usuario interno y devuelve JWT con roles y permisos. */
    @PostMapping("/login")
    public ResponseEntity<JwtResponse> iniciarSesion(@Valid @RequestBody LoginRequest solicitud) {
        return ResponseEntity.ok(authService.iniciarSesion(solicitud));
    }


    /** Devuelve la sesion vigente con roles y permisos actualizados. */
    @GetMapping("/me")
    public ResponseEntity<JwtResponse> obtenerSesionActual(Authentication autenticacion) {
        Usuario usuario = (Usuario) autenticacion.getPrincipal();
        return ResponseEntity.ok(authService.obtenerSesionActual(usuario.getId()));
    }

    /** Cambia el password del usuario autenticado. */
    @PostMapping("/cambiar-password")
    public ResponseEntity<ApiResponse> cambiarPassword(
            Authentication autenticacion,
            @Valid @RequestBody CambioPasswordRequest solicitud
    ) {
        Usuario usuario = (Usuario) autenticacion.getPrincipal();
        authService.cambiarPassword(usuario.getId(), solicitud);
        return ResponseEntity.ok(new ApiResponse("Password actualizado correctamente", true));
    }

    /** Genera un codigo de recuperacion y lo envia por email mock. */
    @PostMapping("/recuperar-password")
    public ResponseEntity<ApiResponse> recuperarPassword(@Valid @RequestBody RecuperarPasswordRequest solicitud) {
        authService.generarCodigoRecuperacion(solicitud);
        return ResponseEntity.ok(new ApiResponse("Codigo de recuperacion generado", true));
    }

    /** Valida el codigo de recuperacion sin cambiar password. */
    @PostMapping("/validar-codigo")
    public ResponseEntity<ApiResponse> validarCodigo(@Valid @RequestBody ValidarCodigoRequest solicitud) {
        authService.validarCodigoRecuperacion(solicitud);
        return ResponseEntity.ok(new ApiResponse("Codigo de recuperacion valido", true));
    }

    /** Verifica el codigo y restablece el password. */
    @PostMapping("/verificar-codigo-recuperacion")
    public ResponseEntity<ApiResponse> verificarCodigoRecuperacion(
            @Valid @RequestBody VerificarCodigoRecuperacionRequest solicitud
    ) {
        authService.restablecerPasswordConCodigo(solicitud);
        return ResponseEntity.ok(new ApiResponse("Password restablecido correctamente", true));
    }

    /** Actualiza el perfil (email, telefono, password) del usuario autenticado. */
    @PutMapping("/mi-perfil")
    public ResponseEntity<UsuarioResponse> actualizarMiPerfil(
            Authentication autenticacion,
            @Valid @RequestBody MiPerfilRequest solicitud
    ) {
        Usuario usuario = (Usuario) autenticacion.getPrincipal();
        return ResponseEntity.ok(authService.actualizarMiPerfil(usuario.getId(), solicitud));
    }
}
