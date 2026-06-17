package com.web.clinica.dto.response;

import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class RolResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private Boolean activo;
    private List<PermisoResponse> permisos;
}
