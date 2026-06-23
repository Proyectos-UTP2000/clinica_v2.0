package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultorioUpdateRequest {

    @NotBlank(message = "El nombre del consultorio es obligatorio")
    private String nombre;

    private String piso;
    private String area;
}
