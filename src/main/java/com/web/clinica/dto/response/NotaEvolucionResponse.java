package com.web.clinica.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class NotaEvolucionResponse {

    private Long id;
    private LocalDateTime fecha;
    private String nota;
    private Long autorId;
    private String autorNombre;
}
