package com.web.clinica.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.List;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
public class JwtProvider {

    @Value("${app.jwt.secret}")
    private String secretoJwt;

    @Value("${app.jwt.expiration-ms}")
    private Long expiracionMs;

    /** Genera un JWT con DNI y autoridades del usuario. */
    public String generarToken(UserDetails usuario) {
        Date ahora = new Date();
        Date expiracion = new Date(ahora.getTime() + expiracionMs);
        List<String> autoridades = usuario.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();

        return Jwts.builder()
                .subject(usuario.getUsername())
                .claim("authorities", autoridades)
                .issuedAt(ahora)
                .expiration(expiracion)
                .signWith(obtenerClave())
                .compact();
    }

    /** Extrae el DNI guardado como subject del token. */
    public String obtenerDni(String token) {
        return obtenerClaims(token).getSubject();
    }

    /** Verifica firma y expiracion del token recibido. */
    public boolean validarToken(String token) {
        obtenerClaims(token);
        return true;
    }

    /** Decodifica los claims firmados del token. */
    private Claims obtenerClaims(String token) {
        return Jwts.parser()
                .verifyWith(obtenerClave())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /** Construye la clave HMAC desde la configuracion Base64/texto. */
    private SecretKey obtenerClave() {
        byte[] bytesSecreto = Decoders.BASE64.decode(secretoJwt);
        return Keys.hmacShaKeyFor(bytesSecreto);
    }
}
