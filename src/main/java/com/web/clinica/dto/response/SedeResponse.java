package com.web.clinica.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SedeResponse {

    private Long id;
    private String nombre;
    private String direccion;
    private Boolean activo;
}
