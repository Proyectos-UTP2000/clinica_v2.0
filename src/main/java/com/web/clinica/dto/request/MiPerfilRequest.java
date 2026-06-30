package com.web.clinica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class MiPerfilRequest {

    @NotBlank(message = "El correo electrónico es obligatorio")
    @Email(message = "El correo electrónico debe ser válido")
    private String email;

    private String telefono;

    private String passwordAnterior;

    private String nuevaPassword;

    private String repetirPassword;
}
