package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class EspecialidadUpdateRequest {

    @NotBlank
    private String nombre;

    private String descripcion;

    private Long especialidadPadreId;
}
