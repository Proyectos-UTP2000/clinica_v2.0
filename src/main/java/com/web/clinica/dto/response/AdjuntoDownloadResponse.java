package com.web.clinica.dto.response;

import org.springframework.core.io.Resource;

public record AdjuntoDownloadResponse(String nombreArchivo, String tipoMime, Resource resource) {
}
