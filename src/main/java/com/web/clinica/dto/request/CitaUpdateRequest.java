package com.web.clinica.dto.request;

import java.time.LocalDateTime;
import lombok.Data;

@Data
public class CitaUpdateRequest {

    private String estado;
    private LocalDateTime nuevaFechaHora;
}
