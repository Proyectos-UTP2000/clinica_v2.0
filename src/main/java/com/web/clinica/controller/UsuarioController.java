package com.web.clinica.controller;

import com.web.clinica.dto.request.UsuarioCreateRequest;
import com.web.clinica.dto.request.UsuarioUpdateRequest;
import com.web.clinica.dto.response.ApiResponse;
import com.web.clinica.dto.response.UsuarioResponse;
import com.web.clinica.security.RequierePermiso;
import com.web.clinica.service.abstractService.IUsuarioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
public class UsuarioController {

    private final IUsuarioService usuarioService;

    @GetMapping
    @RequierePermiso("usuarios.ver")
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioService.listar(pageable);
    }

    @GetMapping("/{id}")
    @RequierePermiso("usuarios.ver")
    public UsuarioResponse obtenerPorId(@PathVariable Long id) {
        return usuarioService.obtenerPorId(id);
    }

    @PostMapping
    @RequierePermiso("usuarios.crear")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody UsuarioCreateRequest solicitud) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crear(solicitud));
    }

    @PutMapping("/{id}")
    @RequierePermiso("usuarios.editar")
    public UsuarioResponse actualizar(@PathVariable Long id, @Valid @RequestBody UsuarioUpdateRequest solicitud) {
        return usuarioService.actualizar(id, solicitud);
    }

    @DeleteMapping("/{id}")
    @RequierePermiso("usuarios.desactivar")
    public ApiResponse desactivar(@PathVariable Long id) {
        usuarioService.desactivar(id);
        return new ApiResponse("Estado de usuario modificado correctamente", true);
    }
}
