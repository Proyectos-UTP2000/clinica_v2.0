package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class SedeUpdateRequest {

    @NotBlank
    private String nombre;

    private String direccion;
}
