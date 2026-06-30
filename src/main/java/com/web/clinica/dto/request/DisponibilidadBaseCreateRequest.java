package com.web.clinica.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.time.LocalTime;
import lombok.Data;

@Data
public class DisponibilidadBaseCreateRequest {

    @NotNull
    private Long sedeId;

    @NotNull
    @Min(1)
    @Max(7)
    private Integer diaSemana;

    @NotNull
    private LocalTime horaInicio;

    @NotNull
    private LocalTime horaFin;

    private Long consultorioId;
}
