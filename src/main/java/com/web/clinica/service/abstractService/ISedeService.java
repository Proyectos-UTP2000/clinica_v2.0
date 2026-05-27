package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.SedeCreateRequest;
import com.web.clinica.dto.request.SedeUpdateRequest;
import com.web.clinica.dto.response.SedeResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ISedeService {

    SedeResponse crear(SedeCreateRequest solicitud);

    SedeResponse actualizar(Long id, SedeUpdateRequest solicitud);

    SedeResponse obtenerPorId(Long id);

    Page<SedeResponse> listarActivos(Pageable pageable);

    void desactivar(Long id);
}
