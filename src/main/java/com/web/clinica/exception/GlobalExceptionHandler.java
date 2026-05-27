package com.web.clinica.exception;

import com.web.clinica.dto.response.ApiResponse;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Responde 400 para reglas de negocio invalidas. */
    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponse> manejarBadRequest(BadRequestException excepcion) {
        return responder(HttpStatus.BAD_REQUEST, excepcion.getMessage());
    }

    /** Responde 401 para credenciales o autenticacion invalida. */
    @ExceptionHandler({UnauthorizedException.class, BadCredentialsException.class})
    public ResponseEntity<ApiResponse> manejarUnauthorized(RuntimeException excepcion) {
        return responder(HttpStatus.UNAUTHORIZED, excepcion.getMessage());
    }

    /** Responde 403 cuando falta un permiso. */
    @ExceptionHandler(AccesoDenegadoException.class)
    public ResponseEntity<ApiResponse> manejarAccesoDenegado(AccesoDenegadoException excepcion) {
        return responder(HttpStatus.FORBIDDEN, excepcion.getMessage());
    }

    /** Responde 404 cuando no existe un recurso solicitado. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiResponse> manejarNoEncontrado(ResourceNotFoundException excepcion) {
        return responder(HttpStatus.NOT_FOUND, excepcion.getMessage());
    }

    /** Responde 400 con detalle de campos invalidos. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse> manejarValidacion(MethodArgumentNotValidException excepcion) {
        String mensaje = excepcion.getBindingResult().getFieldErrors().stream()
                .map(this::formatearCampoInvalido)
                .collect(Collectors.joining("; "));
        return responder(HttpStatus.BAD_REQUEST, mensaje);
    }

    /** Responde 500 para errores no controlados. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse> manejarErrorGenerico(Exception excepcion) {
        return responder(HttpStatus.INTERNAL_SERVER_ERROR, "Ocurrio un error interno");
    }

    /** Construye una respuesta API uniforme. */
    private ResponseEntity<ApiResponse> responder(HttpStatus estado, String mensaje) {
        return ResponseEntity.status(estado).body(new ApiResponse(mensaje, false));
    }

    /** Convierte errores de validacion en texto legible. */
    private String formatearCampoInvalido(FieldError error) {
        return error.getField() + ": " + error.getDefaultMessage();
    }
}
