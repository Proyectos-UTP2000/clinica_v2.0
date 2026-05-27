package com.web.clinica.dto.response;

import java.time.LocalDate;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class PacienteResponse {

    private Long id;
    private String dni;
    private String nombres;
    private String apellidos;
    private String sexo;
    private LocalDate fechaNacimiento;
    private String telefono;
    private String email;
    private Boolean activo;
}
