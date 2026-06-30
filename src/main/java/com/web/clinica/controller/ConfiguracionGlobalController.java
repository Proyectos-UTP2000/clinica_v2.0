package com.web.clinica.controller;

import com.web.clinica.model.ConfiguracionGlobal;
import com.web.clinica.repository.ConfiguracionGlobalRepository;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfiguracionGlobalController {

    private final ConfiguracionGlobalRepository configuracionGlobalRepository;

    @GetMapping
    @RequierePermiso("config.ver")
    public ResponseEntity<List<ConfiguracionGlobal>> obtenerConfiguracion() {
        return ResponseEntity.ok(configuracionGlobalRepository.findAll());
    }

    @PutMapping("/{clave}")
    @RequierePermiso("config.editar")
    public ResponseEntity<ConfiguracionGlobal> actualizarValor(
            @PathVariable String clave,
            @RequestBody Map<String, String> body) {
        String nuevoValor = body.get("valor");
        if (nuevoValor == null) {
            return ResponseEntity.badRequest().build();
        }

        ConfiguracionGlobal config = configuracionGlobalRepository.findById(clave)
                .orElseThrow(() -> new ResourceNotFoundException("Configuración no encontrada para la clave: " + clave));
        
        config.setValor(nuevoValor);
        return ResponseEntity.ok(configuracionGlobalRepository.save(config));
    }
}
