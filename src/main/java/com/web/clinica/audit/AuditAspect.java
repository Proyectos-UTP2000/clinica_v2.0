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
        for (int i = 0; i < args.length; i++) {
            if (args[i] != null) {
                argsString.append("arg[").append(i).append("]: ").append(args[i].toString()).append("; ");
            }
        }

        Object result;
        try {
            result = joinPoint.proceed();
            
            // Log exitoso
            AuditLog log = AuditLog.builder()
                    .usuarioDni(dni)
                    .usuarioNombre(nombre)
                    .accion(auditAction.value())
                    .detalles("Ejecutado con exito. Argumentos: " + argsString.toString())
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
                    .accion(auditAction.value())
                    .detalles("Fallo. Argumentos: " + argsString.toString() + ". Error: " + t.getMessage())
                    .ipAddress(ip)
                    .fecha(LocalDateTime.now())
                    .build();
            auditLogRepository.save(log);
            throw t;
        }
    }
}
