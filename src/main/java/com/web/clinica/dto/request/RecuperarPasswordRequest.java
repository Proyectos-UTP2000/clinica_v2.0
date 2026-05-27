package com.web.clinica.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RecuperarPasswordRequest {

    @NotBlank
    private String dni;

    @Email
    @NotBlank
    private String email;
}
