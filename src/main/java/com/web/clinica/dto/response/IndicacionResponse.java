package com.web.clinica.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class IndicacionResponse {

    private Long id;
    private String tipo;
    private String descripcion;
}
