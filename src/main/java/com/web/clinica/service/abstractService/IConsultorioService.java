package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.ConsultorioCreateRequest;
import com.web.clinica.dto.request.ConsultorioUpdateRequest;
import com.web.clinica.dto.response.ConsultorioResponse;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IConsultorioService {
    ConsultorioResponse crear(ConsultorioCreateRequest solicitud);
    ConsultorioResponse actualizar(Long id, ConsultorioUpdateRequest solicitud);
    ConsultorioResponse obtenerPorId(Long id);
    Page<ConsultorioResponse> listarActivos(Pageable pageable);
    List<ConsultorioResponse> listarPorSede(Long sedeId);
    void desactivar(Long id);
}
