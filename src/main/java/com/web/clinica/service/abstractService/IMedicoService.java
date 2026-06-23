package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.MedicoCreateRequest;
import com.web.clinica.dto.request.MedicoUpdateRequest;
import com.web.clinica.dto.response.MedicoResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface IMedicoService {

    /** Crea un medico con usuario interno y password temporal. */
    MedicoResponse crear(MedicoCreateRequest solicitud);

    /** Actualiza datos del medico y su usuario asociado. */
    MedicoResponse actualizar(Long id, MedicoUpdateRequest solicitud);

    /** Obtiene un medico por identificador. */
    MedicoResponse obtenerPorId(Long id);

    /** Obtiene el perfil medico del usuario autenticado. */
    MedicoResponse obtenerAutenticado();

    /** Lista medicos activos con filtros opcionales. */
    Page<MedicoResponse> listarActivos(String texto, Long especialidadId, Long sedeId, Pageable pageable);

    /** Consulta datos civiles externos para prellenar formularios de medicos. */
    MedicoResponse consultarDni(String dni);

    /** Desactiva el usuario asociado al medico. */
    void desactivar(Long id);
}
