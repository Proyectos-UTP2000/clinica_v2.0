package com.web.clinica.exception;

import lombok.Getter;

@Getter
public class ConflictoHorarioConsultorioException extends RuntimeException {
    public ConflictoHorarioConsultorioException(String mensaje) {
        super(mensaje);
    }
}
