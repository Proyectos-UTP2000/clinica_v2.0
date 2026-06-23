package com.web.clinica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.time.LocalDate;
import lombok.Data;

@Data
public class PacienteCreateRequest {

    @NotBlank
    @Pattern(regexp = "^[0-9]{8}$", message = "El DNI debe tener exactamente 8 dígitos numéricos")
    private String dni;

    @NotBlank
    @Size(min = 2, max = 100, message = "Los nombres deben tener entre 2 y 100 caracteres")
    private String nombres;

    @NotBlank
    @Size(min = 2, max = 100, message = "Los apellidos deben tener entre 2 y 100 caracteres")
    private String apellidos;

    @Pattern(regexp = "^(M|F|Otro)$", message = "El sexo debe ser M, F o Otro")
    private String sexo;

    @NotNull
    @Past(message = "La fecha de nacimiento debe ser en el pasado")
    private LocalDate fechaNacimiento;

    @NotBlank
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "El teléfono debe tener entre 7 y 15 dígitos y puede comenzar con '+'")
    private String telefono;

    @Email
    private String email;

    private String password;
}
