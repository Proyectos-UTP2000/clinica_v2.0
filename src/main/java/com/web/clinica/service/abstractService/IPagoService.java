package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.PagoCreateRequest;
import com.web.clinica.dto.response.PagoResponse;
import java.util.List;

public interface IPagoService {

    PagoResponse registrarPago(PagoCreateRequest solicitud);

    PagoResponse obtenerPorCita(Long citaId);

    List<PagoResponse> listarPorPaciente(Long pacienteId);
}
