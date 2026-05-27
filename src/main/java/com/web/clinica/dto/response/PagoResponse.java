package com.web.clinica.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PagoResponse {

    private Long id;
    private Long citaId;
    private BigDecimal monto;
    private String metodo;
    private LocalDateTime fechaPago;
    private Long registradoPorId;
    private String registradoPor;
}
