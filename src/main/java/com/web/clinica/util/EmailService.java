package com.web.clinica.util;

public interface EmailService {

    /** Envia un correo usando la implementacion configurada. */
    void enviarCorreo(String destinatario, String asunto, String contenido);
}
