package com.web.clinica.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class NotaEvolucionRequest {

    @NotBlank
    private String nota;
}
