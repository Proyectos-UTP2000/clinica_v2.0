package com.web.clinica.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
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
    @FutureOrPresent(message = "La fecha y hora de la cita debe ser presente o futura")
    private LocalDateTime fechaHoraInicio;

    @NotNull(message = "El consultorio es obligatorio")
    private Long consultorioId;

    private Boolean pagoAnticipado;
}
