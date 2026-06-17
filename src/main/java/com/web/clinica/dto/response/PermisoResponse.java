package com.web.clinica.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PermisoResponse {

    private Long id;
    private String codigo;
    private String descripcion;
}
