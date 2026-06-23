package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CitaCreateRequest {

    @NotNull
    private Long pacienteId;

    @NotNull
    private Long doctorId;

    @NotNull
    private Long sedeId;

    @NotNull
    private LocalDateTime fechaHoraInicio;

    @NotNull(message = "El consultorio es obligatorio")
    private Long consultorioId;

    private Boolean pagoAnticipado;
}
