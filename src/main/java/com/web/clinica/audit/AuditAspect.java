package com.web.clinica.audit;

import com.web.clinica.model.AuditLog;
import com.web.clinica.repository.AuditLogRepository;
import com.web.clinica.model.Usuario;
import lombok.RequiredArgsConstructor;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

@Aspect
@Component
@RequiredArgsConstructor
public class AuditAspect {

    private final AuditLogRepository auditLogRepository;

    @Around("@annotation(auditAction)")
    public Object auditar(ProceedingJoinPoint joinPoint, AuditAction auditAction) throws Throwable {
        String dni = "Anonimo";
        String nombre = "Invitado";
        
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof Usuario usuario) {
            dni = usuario.getDni();
            nombre = usuario.getNombres() + " " + usuario.getApellidos();
        }

        String ip = "127.0.0.1";
        try {
            var attributes = RequestContextHolder.getRequestAttributes();
            if (attributes instanceof ServletRequestAttributes servletRequestAttributes) {
                var request = servletRequestAttributes.getRequest();
                ip = request.getHeader("X-Forwarded-For");
                if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
                    ip = request.getRemoteAddr();
                }
            }
        } catch (Exception ignored) {}

        Object[] args = joinPoint.getArgs();
        StringBuilder argsString = new StringBuilder();
        String[] parameterNames = null;
        try {
            org.aspectj.lang.reflect.MethodSignature signature = (org.aspectj.lang.reflect.MethodSignature) joinPoint.getSignature();
            parameterNames = signature.getParameterNames();
        } catch (Exception ignored) {}

        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                String name = (parameterNames != null && parameterNames.length > i) ? parameterNames[i] : ("arg[" + i + "]");
                String value = args[i].toString();
                if (value.length() > 200) {
                    value = value.substring(0, 200) + "...";
                }
                argsString.append(name).append(": ").append(value).append("; ");
            }
        }

        String accion = auditAction.value();
        String detalleAmigable = generarDetalleAmigable(accion, args);
        String detallesTecnicos = "IP: " + ip + " | Parámetros: " + argsString.toString();

        Object result;
        try {
            result = joinPoint.proceed();
            
            // Log exitoso
            AuditLog log = AuditLog.builder()
                    .usuarioDni(dni)
                    .usuarioNombre(nombre)
                    .accion(accion)
                    .estado("EXITOSO")
                    .detalles(detalleAmigable + " || " + detallesTecnicos)
                    .ipAddress(ip)
                    .fecha(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
            
            return result;
        } catch (Throwable t) {
            // Log con fallo
            AuditLog log = AuditLog.builder()
                    .usuarioDni(dni)
                    .usuarioNombre(nombre)
                    .accion(accion)
                    .estado("FALLIDO")
                    .detalles(detalleAmigable + " || " + detallesTecnicos + " | Error: " + t.getMessage())
                    .ipAddress(ip)
                    .fecha(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
            throw t;
        }
    }

    private String generarDetalleAmigable(String accion, Object[] args) {
        try {
            if ("Crear consulta médica".equals(accion) && args.length > 0 && args[0] != null) {
                return "Registro de una nueva consulta médica asociada a la Cita #" + obtenerCitaIdDeRequest(args[0]);
            }
            if ("Ver detalle de consulta médica".equals(accion) && args.length > 0 && args[0] != null) {
                return "Consulta individual de los datos clínicos de la Consulta Médica #" + args[0];
            }
            if ("Ver historial clínico por paciente".equals(accion) && args.length > 0 && args[0] != null) {
                return "Consulta del Historial Clínico completo del Paciente con ID: " + args[0];
            }
            if ("Ver mis consultas médicas como doctor".equals(accion)) {
                return "Búsqueda y visualización de consultas médicas asignadas al profesional autenticado.";
            }
            if ("Agregar nota de evolución".equals(accion) && args.length > 0 && args[0] != null) {
                return "Inserción de nueva nota de evolución en la Consulta Médica #" + args[0];
            }
            if ("Agregar adjunto a consulta".equals(accion) && args.length > 0 && args[0] != null) {
                return "Carga de documento adjunto en la Consulta Médica #" + args[0];
            }
            if ("Descargar PDF de consulta médica".equals(accion) && args.length > 0 && args[0] != null) {
                return "Descarga e impresión del PDF de la Ficha Médica #" + args[0];
            }
        } catch (Exception ignored) {}
        
        return "Operación realizada en el sistema.";
    }

    private String obtenerCitaIdDeRequest(Object request) {
        try {
            java.lang.reflect.Method getCitaId = request.getClass().getMethod("getCitaId");
            Object id = getCitaId.invoke(request);
            return id != null ? id.toString() : "N/D";
        } catch (Exception e) {
            return "N/D";
        }
    }
}
