package com.web.clinica.dto.request;

import lombok.Data;

@Data
public class AdjuntoRequest {

    private String nombreArchivo;
    private String ruta;
    private String tipoMime;
}
