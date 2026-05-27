package com.web.clinica.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CitaResponse {

    private Long id;
    private String pacienteNombre;
    private String doctorNombre;
    private String sedeNombre;
    private LocalDateTime fechaHoraInicio;
    private LocalDateTime fechaHoraFin;
    private String estado;
    private String estadoPago;
    private String origen;
}
