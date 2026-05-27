package com.web.clinica.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EspecialidadResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Long especialidadPadreId;
    private String especialidadPadreNombre;
}
