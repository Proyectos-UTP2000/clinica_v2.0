package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class IndicacionRequest {

    @NotBlank
    private String tipo;

    private String descripcion;
}
