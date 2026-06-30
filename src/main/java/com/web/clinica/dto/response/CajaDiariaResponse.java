package com.web.clinica.dto.response;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class CajaDiariaResponse {
    private Long id;
    private LocalDate fecha;
    private BigDecimal montoApertura;
    private BigDecimal montoCierre;
    private BigDecimal ingresos;
    private BigDecimal egresos;
    private BigDecimal balanceReal;
    private BigDecimal diferencia;
    private String estado;
    private LocalDateTime fechaApertura;
    private LocalDateTime fechaCierre;
    private String abiertoPorNombre;
    private String cerradoPorNombre;
    private String observaciones;
}
