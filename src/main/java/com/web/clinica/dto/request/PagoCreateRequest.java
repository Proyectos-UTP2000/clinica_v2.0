package com.web.clinica.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class PagoCreateRequest {

    @NotNull
    private Long citaId;

    @NotNull
    @DecimalMin(value = "0.00")
    private BigDecimal monto;

    @NotBlank
    private String metodo;
}
