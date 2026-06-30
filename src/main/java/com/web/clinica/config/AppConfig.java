package com.web.clinica.config;

import java.security.SecureRandom;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AppConfig {

    /** Provee generacion segura para codigos temporales. */
    @Bean
    public SecureRandom generadorSeguro() {
        return new SecureRandom();
    }

    /** Codifica passwords con BCrypt. */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @org.springframework.beans.factory.annotation.Value("${app.cloudinary.cloud-name}")
    private String cloudName;

    @org.springframework.beans.factory.annotation.Value("${app.cloudinary.api-key}")
    private String apiKey;

    @org.springframework.beans.factory.annotation.Value("${app.cloudinary.api-secret}")
    private String apiSecret;

    @Bean
    public com.cloudinary.Cloudinary cloudinary() {
        java.util.Map<String, String> config = new java.util.HashMap<>();
        config.put("cloud_name", cloudName);
        config.put("api_key", apiKey);
        config.put("api_secret", apiSecret);
        return new com.cloudinary.Cloudinary(config);
    }
}

