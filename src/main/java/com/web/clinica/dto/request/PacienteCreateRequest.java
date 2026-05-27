package com.web.clinica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PacienteCreateRequest {

    @NotBlank
    private String dni;

    @NotBlank
    private String nombres;

    @NotBlank
    private String apellidos;

    private String sexo;

    @NotNull
    private LocalDate fechaNacimiento;

    @NotBlank
    private String telefono;

    @Email
    private String email;

    private String password;
}
