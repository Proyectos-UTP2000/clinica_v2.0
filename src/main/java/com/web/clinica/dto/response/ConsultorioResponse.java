package com.web.clinica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultorioResponse {
    private Long id;
    private Long sedeId;
    private String sedeNombre;
    private String nombre;
    private String piso;
    private String area;
    private Boolean activo;
}
