package com.web.clinica.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceMock implements EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailServiceMock.class);

    /** Registra el correo en logs mientras no exista proveedor real. */
    @Override
    public void enviarCorreo(String destinatario, String asunto, String contenido) {
        logger.info("Correo mock enviado a {} con asunto '{}': {}", destinatario, asunto, contenido);
    }
}
