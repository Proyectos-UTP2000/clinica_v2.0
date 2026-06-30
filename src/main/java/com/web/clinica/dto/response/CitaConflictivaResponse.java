package com.web.clinica.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CitaConflictivaResponse {
    private Long id;
    private String pacienteDni;
    private String pacienteNombre;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
}
