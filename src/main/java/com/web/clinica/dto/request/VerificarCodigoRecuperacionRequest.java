package com.web.clinica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerificarCodigoRecuperacionRequest {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String codigo;

    @NotBlank
    private String nuevaPassword;

    @NotBlank
    private String repetirPassword;
}
