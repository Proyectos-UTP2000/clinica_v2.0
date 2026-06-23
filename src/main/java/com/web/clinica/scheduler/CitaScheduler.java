package com.web.clinica.scheduler;

import com.web.clinica.model.Cita;
import com.web.clinica.repository.CitaRepository;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
@Slf4j
public class CitaScheduler {

    private final CitaRepository citaRepository;

    // Corre cada 30 minutos (1800000 milisegundos)
    @Scheduled(fixedRate = 1800000)
    @Transactional
    public void procesarCitasVencidas() {
        LocalDateTime ahora = LocalDateTime.now();
        List<String> estadosActivos = Arrays.asList("programada", "reprogramada");
        List<Cita> vencidas = citaRepository.buscarCitasVencidas(ahora, estadosActivos);

        if (!vencidas.isEmpty()) {
            log.info("Iniciando procesamiento de {} citas vencidas para marcar inasistencia", vencidas.size());
            for (Cita cita : vencidas) {
                cita.setEstado("no_asistida");
                citaRepository.save(cita);
                log.info("Cita ID {} (Paciente ID {}) marcada como no_asistida debido a vencimiento", cita.getId(), cita.getPaciente().getId());
            }
        }
    }
}
