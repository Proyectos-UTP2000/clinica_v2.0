package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultorioCreateRequest {

    @NotNull(message = "El ID de la sede es obligatorio")
    private Long sedeId;

    @NotBlank(message = "El nombre del consultorio es obligatorio")
    private String nombre;

    private String piso;
    private String area;
}
