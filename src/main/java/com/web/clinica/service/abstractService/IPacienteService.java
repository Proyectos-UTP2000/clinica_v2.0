package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.PacienteCreateRequest;
import com.web.clinica.dto.request.PacienteUpdateRequest;
import com.web.clinica.dto.response.PacienteResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IPacienteService {

    /** Crea un paciente validando DNI unico. */
    PacienteResponse crear(PacienteCreateRequest solicitud);

    /** Actualiza los datos editables de un paciente. */
    PacienteResponse actualizar(Long id, PacienteUpdateRequest solicitud);

    /** Obtiene un paciente por identificador. */
    PacienteResponse obtenerPorId(Long id);

    /** Lista pacientes activos con paginacion. */
    Page<PacienteResponse> listarActivos(Pageable pageable);

    /** Desactiva un paciente sin borrar su historial. */
    void desactivar(Long id);
}
