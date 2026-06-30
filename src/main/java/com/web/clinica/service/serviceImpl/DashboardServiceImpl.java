package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.response.DashboardTotalesResponse;
import com.web.clinica.repository.CitaRepository;
import com.web.clinica.repository.DoctorRepository;
import com.web.clinica.repository.PacienteRepository;
import com.web.clinica.service.abstractService.IDashboardService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DashboardServiceImpl implements IDashboardService {

    private static final List<String> ESTADOS_CITAS_PROGRAMADAS = List.of("programada", "confirmada");

    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final CitaRepository citaRepository;

    /** Calcula totales principales del dashboard con datos reales. */
    @Override
    @Transactional(readOnly = true)
    public DashboardTotalesResponse obtenerTotales() {
        LocalDate hoy = LocalDate.now();
        LocalDateTime inicioDia = hoy.atStartOfDay();
        LocalDateTime finDia = hoy.plusDays(1).atStartOfDay();

        return DashboardTotalesResponse.builder()
                .totalPacientes(pacienteRepository.countByActivoTrue())
                .totalMedicos(doctorRepository.countDoctoresActivos())
                .totalCitasProgramadas(citaRepository.countByEstadoIn(ESTADOS_CITAS_PROGRAMADAS))
                .citasHoy(citaRepository.countCitasProgramadasEntre(inicioDia, finDia))
                .citasAtendidas(citaRepository.countByEstado("atendida"))
                .citasCanceladas(citaRepository.countByEstado("cancelada"))
                .citasNoAsistidas(citaRepository.countByEstado("no_asistida"))
                .citasReprogramadas(citaRepository.countByEstado("reprogramada"))
                .build();
    }
}
