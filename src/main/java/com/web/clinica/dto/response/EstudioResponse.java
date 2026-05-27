package com.web.clinica.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EstudioResponse {

    private Long id;
    private String tipoEstudio;
    private String detalle;
    private String estado;
    private String archivoResultado;
}
