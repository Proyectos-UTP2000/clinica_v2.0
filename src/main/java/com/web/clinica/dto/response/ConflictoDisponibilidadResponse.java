package com.web.clinica.dto.response;

import java.util.List;
import lombok.Getter;

@Getter
public class ConflictoDisponibilidadResponse extends ApiResponse {
    private final List<CitaConflictivaResponse> citasConflictivas;

    public ConflictoDisponibilidadResponse(String mensaje, List<CitaConflictivaResponse> citasConflictivas) {
        super(mensaje, false);
        this.citasConflictivas = citasConflictivas;
    }
}
