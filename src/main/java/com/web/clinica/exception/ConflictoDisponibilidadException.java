package com.web.clinica.exception;

import com.web.clinica.dto.response.CitaConflictivaResponse;
import java.util.List;
import lombok.Getter;

@Getter
public class ConflictoDisponibilidadException extends RuntimeException {
    private final List<CitaConflictivaResponse> citasConflictivas;

    public ConflictoDisponibilidadException(String mensaje, List<CitaConflictivaResponse> citasConflictivas) {
        super(mensaje);
        this.citasConflictivas = citasConflictivas;
    }
}
