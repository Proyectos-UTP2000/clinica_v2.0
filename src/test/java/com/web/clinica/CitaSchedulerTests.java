package com.web.clinica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.web.clinica.model.Cita;
import com.web.clinica.model.Paciente;
import com.web.clinica.repository.CitaRepository;
import com.web.clinica.scheduler.CitaScheduler;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;

class CitaSchedulerTests {

    @Test
    void procesarCitasVencidasCambiaEstadoANoAsistida() {
        CitaRepository repository = mock(CitaRepository.class);
        CitaScheduler scheduler = new CitaScheduler(repository);

        Paciente paciente = new Paciente();
        paciente.setId(10L);

        Cita cita1 = new Cita();
        cita1.setId(1L);
        cita1.setEstado("programada");
        cita1.setPaciente(paciente);

        Cita cita2 = new Cita();
        cita2.setId(2L);
        cita2.setEstado("reprogramada");
        cita2.setPaciente(paciente);

        when(repository.buscarCitasVencidas(any(LocalDateTime.class), eq(Arrays.asList("programada", "reprogramada"))))
                .thenReturn(Arrays.asList(cita1, cita2));

        scheduler.procesarCitasVencidas();

        assertThat(cita1.getEstado()).isEqualTo("no_asistida");
        assertThat(cita2.getEstado()).isEqualTo("no_asistida");
        verify(repository, times(1)).save(cita1);
        verify(repository, times(1)).save(cita2);
    }
}
