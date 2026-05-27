package com.web.clinica.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RecetaResponse {

    private Long id;
    private String medicamento;
    private String dosis;
    private String frecuencia;
    private String duracion;
    private String indicaciones;
}
