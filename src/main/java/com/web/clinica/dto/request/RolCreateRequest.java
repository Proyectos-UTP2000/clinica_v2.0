package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class RolCreateRequest {

    @NotBlank
    @Size(max = 50)
    private String nombre;

    @Size(max = 255)
    private String descripcion;

    private List<Long> permisosIds = new ArrayList<>();
}
