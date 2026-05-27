package com.web.clinica.dto.response;

import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class AdjuntoResponse {

    private Long id;
    private String nombreArchivo;
    private String ruta;
    private String tipoMime;
    private LocalDateTime fechaSubida;
}
