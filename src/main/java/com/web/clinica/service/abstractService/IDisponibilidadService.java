package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.DisponibilidadBaseCreateRequest;
import com.web.clinica.dto.request.ExcepcionDisponibilidadCreateRequest;
import com.web.clinica.dto.response.DisponibilidadBaseResponse;
import com.web.clinica.dto.response.ExcepcionDisponibilidadResponse;
import java.time.LocalDate;
import java.util.List;

public interface IDisponibilidadService {

    List<DisponibilidadBaseResponse> listarBases(Long doctorId);

    DisponibilidadBaseResponse guardarBase(Long doctorId, DisponibilidadBaseCreateRequest solicitud);

    void eliminarBase(Long doctorId, Long disponibilidadId);

    List<ExcepcionDisponibilidadResponse> listarExcepciones(Long doctorId, LocalDate fechaInicio, LocalDate fechaFin);

    ExcepcionDisponibilidadResponse crearExcepcion(Long doctorId, ExcepcionDisponibilidadCreateRequest solicitud);

    void eliminarExcepcion(Long doctorId, Long excepcionId);
}
