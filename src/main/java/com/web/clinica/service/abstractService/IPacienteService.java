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

    /** Busca un paciente registrado por DNI. */
    PacienteResponse buscarPorDni(String dni);

    /** Consulta datos civiles externos para prellenar formularios. */
    PacienteResponse consultarDni(String dni);

    /** Lista pacientes activos con paginacion y filtro opcional de busqueda. */
    Page<PacienteResponse> listarActivos(String buscar, Pageable pageable);

    /** Desactiva un paciente sin borrar su historial. */
    void desactivar(Long id);
}
