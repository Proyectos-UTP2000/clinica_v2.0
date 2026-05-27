package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CambioPasswordRequest {

    @NotBlank
    private String nuevaPassword;

    @NotBlank
    private String repetirPassword;
}
