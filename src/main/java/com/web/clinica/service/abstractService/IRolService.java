package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.AsignarPermisosRequest;
import com.web.clinica.dto.request.RolCreateRequest;
import com.web.clinica.dto.request.RolUpdateRequest;
import com.web.clinica.dto.response.RolResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IRolService {

    Page<RolResponse> listar(Pageable pageable);

    RolResponse obtenerPorId(Long id);

    RolResponse crear(RolCreateRequest solicitud);

    RolResponse actualizar(Long id, RolUpdateRequest solicitud);

    RolResponse asignarPermisos(Long id, AsignarPermisosRequest solicitud);

    void desactivar(Long id);
}
