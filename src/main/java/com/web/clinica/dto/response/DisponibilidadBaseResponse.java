package com.web.clinica.dto.response;

import java.time.LocalTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DisponibilidadBaseResponse {

    private Long id;
    private Long doctorId;
    private Long sedeId;
    private String sedeNombre;
    private Integer diaSemana;
    private LocalTime horaInicio;
    private LocalTime horaFin;
}
