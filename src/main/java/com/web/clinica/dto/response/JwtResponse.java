package com.web.clinica.dto.response;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class JwtResponse {

    private String token;
    private String dni;
    private String nombres;
    private String apellidos;
    private boolean cambioPasswordObligatorio;
    private List<String> roles;
    private List<String> permisos;
}
