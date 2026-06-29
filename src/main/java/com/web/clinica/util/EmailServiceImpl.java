package com.web.clinica.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String remitente;

    @Value("${app.mail.enabled:false}")
    private boolean habilitado;

    @Override
    public void enviarCorreo(String destinatario, String asunto, String contenido) {
        if (!habilitado) {
            log.info("[MOCK MAIL] Destinatario: {}, Asunto: {}, Contenido: {}", destinatario, asunto, contenido);
            return;
        }
        try {
            SimpleMailMessage mensaje = new SimpleMailMessage();
            mensaje.setFrom(remitente);
            mensaje.setTo(destinatario);
            mensaje.setSubject(asunto);
            mensaje.setText(contenido);
            mailSender.send(mensaje);
            log.info("Correo real enviado exitosamente a {}", destinatario);
        } catch (Exception e) {
            log.error("Error al enviar correo real a {}: {}", destinatario, e.getMessage(), e);
        }
    }
}
