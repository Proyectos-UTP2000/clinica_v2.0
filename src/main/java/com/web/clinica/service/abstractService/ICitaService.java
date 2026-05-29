package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.CitaCreateRequest;
import com.web.clinica.dto.response.CitaResponse;
import com.web.clinica.dto.response.DisponibilidadSlotResponse;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ICitaService {

    /** Agenda una cita validando disponibilidad. */
    CitaResponse crear(CitaCreateRequest solicitud);

    /** Reprograma una cita si quedan intentos disponibles. */
    CitaResponse reprogramar(Long citaId, LocalDateTime nuevaFechaHora);

    /** Cancela una cita vigente. */
    void cancelar(Long citaId);

    /** Obtiene una cita por identificador. */
    CitaResponse obtenerPorId(Long id);

    /** Lista citas con filtros internos opcionales. */
    Page<CitaResponse> listarConFiltros(Long pacienteId,
                                        Long doctorId,
                                        Long sedeId,
                                        LocalDate fecha,
                                        LocalDate fechaInicio,
                                        LocalDate fechaFin,
                                        Pageable pageable);

    /** Lista citas de un paciente. */
    Page<CitaResponse> listarPorPaciente(Long pacienteId, Pageable pageable);

    /** Lista citas de un doctor con fecha opcional. */
    Page<CitaResponse> listarPorDoctor(Long doctorId,
                                       Pageable pageable,
                                       LocalDate fecha,
                                       LocalDate fechaInicio,
                                       LocalDate fechaFin);

    /** Lista citas del doctor autenticado. */
    Page<CitaResponse> listarMisCitas(Pageable pageable, LocalDate fecha, LocalDate fechaInicio, LocalDate fechaFin);

    /** Calcula slots libres de 30 minutos. */
    List<DisponibilidadSlotResponse> obtenerSlotsDisponibles(Long doctorId, Long sedeId, LocalDate fecha);
}
