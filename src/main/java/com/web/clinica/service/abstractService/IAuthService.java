package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.CambioPasswordRequest;
import com.web.clinica.dto.request.LoginRequest;
import com.web.clinica.dto.request.MiPerfilRequest;
import com.web.clinica.dto.request.RecuperarPasswordRequest;
import com.web.clinica.dto.request.ValidarCodigoRequest;
import com.web.clinica.dto.request.VerificarCodigoRecuperacionRequest;
import com.web.clinica.dto.response.JwtResponse;
import com.web.clinica.dto.response.UsuarioResponse;

public interface IAuthService {

    /** Actualiza los datos de contacto y password del usuario autenticado. */
    UsuarioResponse actualizarMiPerfil(Long usuarioId, MiPerfilRequest solicitud);

    /** Autentica por DNI y password, y devuelve el JWT. */
    JwtResponse iniciarSesion(LoginRequest solicitud);

    /** Reconstruye la sesion del usuario autenticado con roles y permisos vigentes. */
    JwtResponse obtenerSesionActual(Long usuarioId);

    /** Cambia el password del usuario autenticado. */
    void cambiarPassword(Long usuarioId, CambioPasswordRequest solicitud);

    /** Genera un codigo temporal para recuperar password. */
    void generarCodigoRecuperacion(RecuperarPasswordRequest solicitud);

    /** Valida que un codigo de recuperacion sea vigente y no usado. */
    void validarCodigoRecuperacion(ValidarCodigoRequest solicitud);

    /** Restablece el password usando un codigo vigente. */
    void restablecerPasswordConCodigo(VerificarCodigoRecuperacionRequest solicitud);
}
