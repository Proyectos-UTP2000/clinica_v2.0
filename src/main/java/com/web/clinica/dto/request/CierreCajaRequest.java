package com.web.clinica.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import lombok.Data;

@Data
public class CierreCajaRequest {

    @NotNull(message = "El balance real es obligatorio")
    @DecimalMin(value = "0.00", message = "El balance real no puede ser negativo")
    private BigDecimal balanceReal;

    private String observaciones;
}
