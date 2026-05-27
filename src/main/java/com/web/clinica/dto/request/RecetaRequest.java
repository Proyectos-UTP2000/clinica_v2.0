package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class RecetaRequest {

    @NotBlank
    private String medicamento;

    private String dosis;
    private String frecuencia;
    private String duracion;
    private String indicaciones;
}
