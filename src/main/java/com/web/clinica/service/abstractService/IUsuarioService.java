package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.UsuarioCreateRequest;
import com.web.clinica.dto.request.UsuarioUpdateRequest;
import com.web.clinica.dto.response.UsuarioResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IUsuarioService {

    Page<UsuarioResponse> listar(Pageable pageable);

    UsuarioResponse obtenerPorId(Long id);

    UsuarioResponse crear(UsuarioCreateRequest solicitud);

    UsuarioResponse actualizar(Long id, UsuarioUpdateRequest solicitud);

    void desactivar(Long id);
}
