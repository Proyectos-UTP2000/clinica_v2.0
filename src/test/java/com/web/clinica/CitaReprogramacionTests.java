package com.web.clinica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.web.clinica.dto.response.CitaResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.model.Cita;
import com.web.clinica.model.DisponibilidadBase;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Especialidad;
import com.web.clinica.model.Paciente;
import com.web.clinica.model.Sede;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.CitaRepository;
import com.web.clinica.repository.DisponibilidadBaseRepository;
import com.web.clinica.repository.DoctorRepository;
import com.web.clinica.repository.ExcepcionDisponibilidadRepository;
import com.web.clinica.repository.PacienteRepository;
import com.web.clinica.repository.SecretariaRepository;
import com.web.clinica.repository.SedeRepository;
import com.web.clinica.service.serviceImpl.CitaServiceImpl;
import com.web.clinica.util.EmailService;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

class CitaReprogramacionTests {

    private CitaRepository citaRepository;
    private DoctorRepository doctorRepository;
    private DisponibilidadBaseRepository disponibilidadBaseRepository;
    private ExcepcionDisponibilidadRepository excepcionDisponibilidadRepository;
    private SecretariaRepository secretariaRepository;
    private CitaServiceImpl service;

    @BeforeEach
    void configurarServicio() {
        citaRepository = mock(CitaRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        disponibilidadBaseRepository = mock(DisponibilidadBaseRepository.class);
        excepcionDisponibilidadRepository = mock(ExcepcionDisponibilidadRepository.class);
        secretariaRepository = mock(SecretariaRepository.class);
        service = new CitaServiceImpl(
                citaRepository,
                mock(PacienteRepository.class),
                doctorRepository,
                mock(SedeRepository.class),
                disponibilidadBaseRepository,
                excepcionDisponibilidadRepository,
                secretariaRepository,
                mock(EmailService.class)
        );
    }

    @AfterEach
    void limpiarSeguridad() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void reprogramarPermiteCambiarDoctorConMismaEspecialidadYDisponibilidad() {
        Especialidad cardiologia = especialidad(1L, "Cardiologia");
        Sede sede = sede(1L, "Central");
        Doctor doctorActual = doctor(1L, "Ana", "Ruiz", cardiologia, sede, 10L);
        Doctor doctorNuevo = doctor(2L, "Luis", "Perez", cardiologia, sede, 20L);
        Cita cita = cita(doctorActual, sede);
        LocalDateTime nuevaFecha = LocalDateTime.of(2026, 6, 22, 10, 0);
        autenticarDoctor(doctorActual.getUsuario());

        when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctorNuevo));
        when(disponibilidadBaseRepository.findByDoctorAndSedeAndDiaSemana(doctorNuevo, sede, 1))
                .thenReturn(List.of(disponibilidad(doctorNuevo, sede)));
        when(excepcionDisponibilidadRepository.findByDoctorAndFecha(any(), any())).thenReturn(List.of());
        when(citaRepository.findByDoctorAndFechaHoraInicioBetween(any(), any(), any())).thenReturn(List.of());
        when(citaRepository.findByPacienteAndFechaHoraInicioBetween(any(), any(), any())).thenReturn(List.of());
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaResponse respuesta = service.reprogramar(5L, nuevaFecha, 2L);

        assertThat(cita.getDoctor()).isEqualTo(doctorNuevo);
        assertThat(cita.getFechaHoraInicio()).isEqualTo(nuevaFecha);
        assertThat(cita.getReprogramacionesRestantes()).isEqualTo(1);
        assertThat(respuesta.getDoctorNombre()).isEqualTo("Luis Perez");
    }

    @Test
    void reprogramarRechazaCambiarDoctorDeOtraEspecialidad() {
        Especialidad cardiologia = especialidad(1L, "Cardiologia");
        Especialidad pediatria = especialidad(2L, "Pediatria");
        Sede sede = sede(1L, "Central");
        Doctor doctorActual = doctor(1L, "Ana", "Ruiz", cardiologia, sede, 10L);
        Doctor doctorNuevo = doctor(2L, "Luis", "Perez", pediatria, sede, 20L);
        Cita cita = cita(doctorActual, sede);
        autenticarDoctor(doctorActual.getUsuario());

        when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));
        when(doctorRepository.findById(2L)).thenReturn(Optional.of(doctorNuevo));

        assertThatThrownBy(() -> service.reprogramar(5L, LocalDateTime.of(2026, 6, 22, 10, 0), 2L))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("misma especialidad");
    }

    @Test
    void reprogramarCitaNoAsistidaSinPagoAnticipadoLanzaExcepcion() {
        Especialidad cardiologia = especialidad(1L, "Cardiologia");
        Sede sede = sede(1L, "Central");
        Doctor doctorActual = doctor(1L, "Ana", "Ruiz", cardiologia, sede, 10L);
        Cita cita = cita(doctorActual, sede);
        cita.setEstado("no_asistida");
        cita.setPagoAnticipado(false);
        autenticarDoctor(doctorActual.getUsuario());

        when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));

        assertThatThrownBy(() -> service.reprogramar(5L, LocalDateTime.of(2026, 6, 22, 10, 0), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("sin pago anticipado");
    }

    @Test
    void reprogramarCitaNoAsistidaConPagoAnticipadoExito() {
        Especialidad cardiologia = especialidad(1L, "Cardiologia");
        Sede sede = sede(1L, "Central");
        Doctor doctorActual = doctor(1L, "Ana", "Ruiz", cardiologia, sede, 10L);
        Cita cita = cita(doctorActual, sede);
        cita.setEstado("no_asistida");
        cita.setPagoAnticipado(true);
        cita.setReprogramacionesRestantes(2);
        autenticarDoctor(doctorActual.getUsuario());

        when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));
        when(disponibilidadBaseRepository.findByDoctorAndSedeAndDiaSemana(doctorActual, sede, 1))
                .thenReturn(List.of(disponibilidad(doctorActual, sede)));
        when(excepcionDisponibilidadRepository.findByDoctorAndFecha(any(), any())).thenReturn(List.of());
        when(citaRepository.findByDoctorAndFechaHoraInicioBetween(any(), any(), any())).thenReturn(List.of());
        when(citaRepository.findByPacienteAndFechaHoraInicioBetween(any(), any(), any())).thenReturn(List.of());
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaResponse respuesta = service.reprogramar(5L, LocalDateTime.of(2026, 6, 22, 10, 0), null);

        assertThat(respuesta).isNotNull();
        assertThat(cita.getEstado()).isEqualTo("reprogramada");
        assertThat(cita.getReprogramacionesRestantes()).isEqualTo(1);
    }

    @Test
    void reprogramarCitaCanceladaSinPagoAnticipadoLanzaExcepcion() {
        Especialidad cardiologia = especialidad(1L, "Cardiologia");
        Sede sede = sede(1L, "Central");
        Doctor doctorActual = doctor(1L, "Ana", "Ruiz", cardiologia, sede, 10L);
        Cita cita = cita(doctorActual, sede);
        cita.setEstado("cancelada");
        cita.setPagoAnticipado(false);
        autenticarDoctor(doctorActual.getUsuario());

        when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));

        assertThatThrownBy(() -> service.reprogramar(5L, LocalDateTime.of(2026, 6, 22, 10, 0), null))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("estado actual");
    }

    @Test
    void reprogramarCitaCanceladaConPagoAnticipadoExito() {
        Especialidad cardiologia = especialidad(1L, "Cardiologia");
        Sede sede = sede(1L, "Central");
        Doctor doctorActual = doctor(1L, "Ana", "Ruiz", cardiologia, sede, 10L);
        Cita cita = cita(doctorActual, sede);
        cita.setEstado("cancelada");
        cita.setPagoAnticipado(true);
        cita.setReprogramacionesRestantes(2);
        autenticarDoctor(doctorActual.getUsuario());

        when(citaRepository.findById(5L)).thenReturn(Optional.of(cita));
        when(disponibilidadBaseRepository.findByDoctorAndSedeAndDiaSemana(doctorActual, sede, 1))
                .thenReturn(List.of(disponibilidad(doctorActual, sede)));
        when(excepcionDisponibilidadRepository.findByDoctorAndFecha(any(), any())).thenReturn(List.of());
        when(citaRepository.findByDoctorAndFechaHoraInicioBetween(any(), any(), any())).thenReturn(List.of());
        when(citaRepository.findByPacienteAndFechaHoraInicioBetween(any(), any(), any())).thenReturn(List.of());
        when(citaRepository.save(any(Cita.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CitaResponse respuesta = service.reprogramar(5L, LocalDateTime.of(2026, 6, 22, 10, 0), null);

        assertThat(respuesta).isNotNull();
        assertThat(cita.getEstado()).isEqualTo("reprogramada");
        assertThat(cita.getReprogramacionesRestantes()).isEqualTo(1);
    }

    private Cita cita(Doctor doctor, Sede sede) {
        Paciente paciente = Paciente.builder()
                .id(1L)
                .nombres("Yovana")
                .apellidos("Mamani")
                .dni("70135060")
                .telefono("999999999")
                .activo(true)
                .build();
        Cita cita = new Cita();
        cita.setId(5L);
        cita.setPaciente(paciente);
        cita.setDoctor(doctor);
        cita.setSede(sede);
        cita.setFechaHoraInicio(LocalDateTime.of(2026, 6, 22, 9, 0));
        cita.setFechaHoraFin(LocalDateTime.of(2026, 6, 22, 9, 30));
        cita.setEstado("programada");
        cita.setEstadoPago("pendiente");
        cita.setOrigen("interno");
        cita.setReprogramacionesRestantes(2);
        return cita;
    }

    private Doctor doctor(Long id, String nombres, String apellidos, Especialidad especialidad, Sede sede, Long usuarioId) {
        Usuario usuario = new Usuario();
        usuario.setId(usuarioId);
        usuario.setNombres(nombres);
        usuario.setApellidos(apellidos);
        usuario.setDni("0000000" + id);
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setUsuario(usuario);
        doctor.setEspecialidad(especialidad);
        doctor.getSedes().add(sede);
        return doctor;
    }

    private Especialidad especialidad(Long id, String nombre) {
        return Especialidad.builder().id(id).nombre(nombre).build();
    }

    private Sede sede(Long id, String nombre) {
        return Sede.builder().id(id).nombre(nombre).activo(true).build();
    }

    private DisponibilidadBase disponibilidad(Doctor doctor, Sede sede) {
        DisponibilidadBase disponibilidad = new DisponibilidadBase();
        disponibilidad.setDoctor(doctor);
        disponibilidad.setSede(sede);
        disponibilidad.setDiaSemana(1);
        disponibilidad.setHoraInicio(LocalTime.of(8, 0));
        disponibilidad.setHoraFin(LocalTime.of(12, 0));
        return disponibilidad;
    }

    private void autenticarDoctor(Usuario usuario) {
        SecurityContextHolder.getContext().setAuthentication(new UsernamePasswordAuthenticationToken(
                usuario,
                null,
                List.of(new SimpleGrantedAuthority("citas.editar_propias"))
        ));
    }
}
