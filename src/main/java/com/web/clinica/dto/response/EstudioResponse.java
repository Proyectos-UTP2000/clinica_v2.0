package com.web.clinica.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EstudioResponse {


    private Long id;
    private String tipoEstudio;
    private String detalle;
    private String estado;
    private String archivoResultado;
    private String pacienteNombre;
    private String pacienteDni;
    private Long consultaId;
    private java.time.LocalDateTime fechaHora;
}

