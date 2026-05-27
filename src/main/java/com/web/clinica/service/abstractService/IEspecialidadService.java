package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.EspecialidadCreateRequest;
import com.web.clinica.dto.request.EspecialidadUpdateRequest;
import com.web.clinica.dto.response.EspecialidadResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IEspecialidadService {

    EspecialidadResponse crear(EspecialidadCreateRequest solicitud);

    EspecialidadResponse actualizar(Long id, EspecialidadUpdateRequest solicitud);

    EspecialidadResponse obtenerPorId(Long id);

    Page<EspecialidadResponse> listar(Pageable pageable);

    List<EspecialidadResponse> listarTodas();

    void eliminar(Long id);
}
