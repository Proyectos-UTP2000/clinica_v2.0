package com.web.clinica.dto.request;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CitaUpdateRequest {

    private String estado;

    @NotNull(message = "Debe indicar la nueva fecha y hora")
    @FutureOrPresent(message = "La nueva fecha y hora debe ser presente o futura")
    private LocalDateTime nuevaFechaHora;

    private Long doctorId;
}
