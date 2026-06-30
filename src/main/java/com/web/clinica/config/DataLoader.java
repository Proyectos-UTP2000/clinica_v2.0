package com.web.clinica.config;

import com.web.clinica.model.*;
import com.web.clinica.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.Optional;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final SedeRepository sedeRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ConsultorioRepository consultorioRepository;
    private final DisponibilidadBaseRepository disponibilidadBaseRepository;
    private final CitaRepository citaRepository;
    private final ConsultaRepository consultaRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos de prueba...");

        // 1. Asegurar roles y usuarios con contraseñas legibles (todos con Password123)
        String defaultPasswordHash = passwordEncoder.encode("Password123");

        // Administrador
        Rol rolAdmin = rolRepository.findByNombre("Administrador")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("Administrador");
                    r.setDescripcion("Rol administrador");
                    r.setActivo(true);
                    return rolRepository.save(r);
                });

        Usuario admin = usuarioRepository.findByEmail("admin@clinica.com")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setDni("00000000");
                    u.setNombres("Admin");
                    u.setApellidos("Sistema");
                    u.setEmail("admin@clinica.com");
                    u.setActivo(true);
                    return u;
                });
        admin.setPasswordHash(defaultPasswordHash);
        admin.setCambioPasswordObligatorio(false);
        if (admin.getRoles() == null || admin.getRoles().isEmpty()) {
            admin.setRoles(new HashSet<>(Collections.singletonList(rolAdmin)));
        }
        admin = usuarioRepository.save(admin);

        // Médico
        Rol rolDoctor = rolRepository.findByNombre("Doctor")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("Doctor");
                    r.setDescripcion("Rol médico");
                    r.setActivo(true);
                    return rolRepository.save(r);
                });

        Usuario doctorUsuarioTmp = usuarioRepository.findByEmail("diana.medico@clinica.com")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setDni("11111111");
                    u.setNombres("Diana");
                    u.setApellidos("Medico");
                    u.setEmail("diana.medico@clinica.com");
                    u.setTelefono("999111222");
                    u.setFechaNacimiento(LocalDate.of(1985, 3, 12));
                    u.setActivo(true);
                    return u;
                });
        doctorUsuarioTmp.setPasswordHash(defaultPasswordHash);
        doctorUsuarioTmp.setCambioPasswordObligatorio(false);
        if (doctorUsuarioTmp.getRoles() == null || doctorUsuarioTmp.getRoles().isEmpty()) {
            doctorUsuarioTmp.setRoles(new HashSet<>(Collections.singletonList(rolDoctor)));
        }
        final Usuario doctorUsuario = usuarioRepository.save(doctorUsuarioTmp);

        // Secretaria
        Rol rolSecretaria = rolRepository.findByNombre("Secretaria")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("Secretaria");
                    r.setDescripcion("Rol secretaria");
                    r.setActivo(true);
                    return rolRepository.save(r);
                });

        Usuario secUsuario = usuarioRepository.findByEmail("sara.secretaria@clinica.com")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setDni("22222222");
                    u.setNombres("Sara");
                    u.setApellidos("Secretaria");
                    u.setEmail("sara.secretaria@clinica.com");
                    u.setTelefono("999222333");
                    u.setFechaNacimiento(LocalDate.of(1990, 7, 20));
                    u.setActivo(true);
                    return u;
                });
        secUsuario.setPasswordHash(defaultPasswordHash);
        secUsuario.setCambioPasswordObligatorio(false);
        if (secUsuario.getRoles() == null || secUsuario.getRoles().isEmpty()) {
            secUsuario.setRoles(new HashSet<>(Collections.singletonList(rolSecretaria)));
        }
        usuarioRepository.save(secUsuario);

        // Enfermera
        Rol rolEnfermera = rolRepository.findByNombre("Enfermera")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("Enfermera");
                    r.setDescripcion("Rol enfermera");
                    r.setActivo(true);
                    return rolRepository.save(r);
                });

        Usuario enfUsuario = usuarioRepository.findByEmail("elena.enfermera@clinica.com")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setDni("33333333");
                    u.setNombres("Elena");
                    u.setApellidos("Enfermera");
                    u.setEmail("elena.enfermera@clinica.com");
                    u.setTelefono("999333444");
                    u.setFechaNacimiento(LocalDate.of(1992, 11, 5));
                    u.setActivo(true);
                    return u;
                });
        enfUsuario.setPasswordHash(defaultPasswordHash);
        enfUsuario.setCambioPasswordObligatorio(false);
        if (enfUsuario.getRoles() == null || enfUsuario.getRoles().isEmpty()) {
            enfUsuario.setRoles(new HashSet<>(Collections.singletonList(rolEnfermera)));
        }
        usuarioRepository.save(enfUsuario);

        // 2. Sedes, Especialidades y Consultorios
        Sede sedeCentral = sedeRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Sede s = new Sede();
                    s.setNombre("Sede Central");
                    s.setDireccion("Av. Salud 123, Lima");
                    s.setActivo(true);
                    return sedeRepository.save(s);
                });

        Especialidad espGeneral = especialidadRepository.findAll().stream().findFirst()
                .orElseGet(() -> {
                    Especialidad e = new Especialidad();
                    e.setNombre("Medicina General");
                    e.setDescripcion("Atención primaria y control general");
                    return especialidadRepository.save(e);
                });

        Consultorio consultorio1 = consultorioRepository.findBySedeIdAndNombreIgnoreCase(sedeCentral.getId(), "Consultorio 101")
                .orElseGet(() -> {
                    Consultorio c = Consultorio.builder()
                            .sede(sedeCentral)
                            .nombre("Consultorio 101")
                            .piso("1")
                            .area("Medicina General")
                            .activo(true)
                            .build();
                    return consultorioRepository.save(c);
                });

        // 3. Crear Entidad Doctor
        Doctor doctor = doctorRepository.findByUsuarioDni(doctorUsuario.getDni())
                .orElseGet(() -> {
                    Doctor d = new Doctor();
                    d.setUsuario(doctorUsuario);
                    d.setEspecialidad(espGeneral);
                    d.setSedes(new HashSet<>(Collections.singletonList(sedeCentral)));
                    d.setConsultorios(new HashSet<>(Collections.singletonList(consultorio1)));
                    return doctorRepository.save(d);
                });

        // Asegurar que atiende en Sede Central
        if (doctor.getSedes() == null || doctor.getSedes().isEmpty()) {
            doctor.setSedes(new HashSet<>(Collections.singletonList(sedeCentral)));
            doctorRepository.save(doctor);
        }

        // Asegurar que tiene asignado el consultorio
        if (doctor.getConsultorios() == null || doctor.getConsultorios().isEmpty()) {
            doctor.setConsultorios(new HashSet<>(Collections.singletonList(consultorio1)));
            doctorRepository.save(doctor);
        }

        // 4. Disponibilidad Base para el Médico (Lunes a Viernes 8:00 a 13:00)
        if (disponibilidadBaseRepository.findByDoctorIdOrderByDiaSemanaAscHoraInicioAsc(doctor.getId()).isEmpty()) {
            for (int i = 1; i <= 5; i++) {
                DisponibilidadBase db = new DisponibilidadBase();
                db.setDoctor(doctor);
                db.setSede(sedeCentral);
                db.setDiaSemana(i);
                db.setHoraInicio(LocalTime.of(8, 0));
                db.setHoraFin(LocalTime.of(13, 0));
                db.setConsultorio(consultorio1);
                disponibilidadBaseRepository.save(db);
            }
        }

        // 5. Paciente de prueba
        Paciente paciente = pacienteRepository.findByDni("44444444")
                .orElseGet(() -> {
                    Paciente p = new Paciente();
                    p.setDni("44444444");
                    p.setNombres("Pablo");
                    p.setApellidos("Paciente");
                    p.setSexo("M");
                    p.setFechaNacimiento(LocalDate.of(1995, 1, 10));
                    p.setTelefono("988777666");
                    p.setEmail("pablo.paciente@example.com");
                    p.setActivo(true);
                    return p;
                });
        paciente.setPasswordHash(defaultPasswordHash);
        paciente = pacienteRepository.save(paciente);

        // 6. Citas de prueba
        if (citaRepository.count() <= 1) {
            LocalDate hoy = LocalDate.now();

            // Cita 1: Completada/Atendida (de hace dos días)
            Cita citaAtendida = new Cita();
            citaAtendida.setPaciente(paciente);
            citaAtendida.setDoctor(doctor);
            citaAtendida.setSede(sedeCentral);
            citaAtendida.setConsultorio(consultorio1);
            citaAtendida.setFechaHoraInicio(LocalDateTime.of(hoy.minusDays(2), LocalTime.of(9, 0)));
            citaAtendida.setFechaHoraFin(LocalDateTime.of(hoy.minusDays(2), LocalTime.of(9, 30)));
            citaAtendida.setEstado("atendida");
            citaAtendida.setEstadoPago("pagado");
            citaAtendida.setPagoAnticipado(true);
            citaAtendida.setOrigen("interno");
            citaAtendida.setCreadoPorUsuario(admin);
            citaAtendida = citaRepository.save(citaAtendida);

            // Crear la consulta clínica asociada a esta cita atendida
            if (consultaRepository.findByPacienteId(paciente.getId(), org.springframework.data.domain.Pageable.unpaged()).isEmpty()) {
                Consulta consulta = Consulta.builder()
                        .paciente(paciente)
                        .doctor(doctor)
                        .sede(sedeCentral)
                        .cita(citaAtendida)
                        .fechaHora(LocalDateTime.of(hoy.minusDays(2), LocalTime.of(9, 35)))
                        .tipo("consulta")
                        .motivoConsulta("Paciente presenta dolor de cabeza recurrente")
                        .diagnostico("Cefalea tensional leve")
                        .observaciones("Se recomienda descanso e hidratación")
                        .estado("activa")
                        .build();
                consultaRepository.save(consulta);
            }

            // Cita 2: Programada para hoy (10:00 AM)
            Cita citaProgramada = new Cita();
            citaProgramada.setPaciente(paciente);
            citaProgramada.setDoctor(doctor);
            citaProgramada.setSede(sedeCentral);
            citaProgramada.setConsultorio(consultorio1);
            citaProgramada.setFechaHoraInicio(LocalDateTime.of(hoy, LocalTime.of(10, 0)));
            citaProgramada.setFechaHoraFin(LocalDateTime.of(hoy, LocalTime.of(10, 30)));
            citaProgramada.setEstado("programada");
            citaProgramada.setEstadoPago("pendiente");
            citaProgramada.setPagoAnticipado(false);
            citaProgramada.setOrigen("interno");
            citaProgramada.setCreadoPorUsuario(admin);
            citaRepository.save(citaProgramada);

            // Cita 3: Reprogramada para mañana (11:00 AM)
            Cita citaReprogramada = new Cita();
            citaReprogramada.setPaciente(paciente);
            citaReprogramada.setDoctor(doctor);
            citaReprogramada.setSede(sedeCentral);
            citaReprogramada.setConsultorio(consultorio1);
            citaReprogramada.setFechaHoraInicio(LocalDateTime.of(hoy.plusDays(1), LocalTime.of(11, 0)));
            citaReprogramada.setFechaHoraFin(LocalDateTime.of(hoy.plusDays(1), LocalTime.of(11, 30)));
            citaReprogramada.setEstado("reprogramada");
            citaReprogramada.setEstadoPago("pagado");
            citaReprogramada.setPagoAnticipado(true);
            citaReprogramada.setOrigen("interno");
            citaReprogramada.setCreadoPorUsuario(admin);
            citaRepository.save(citaReprogramada);

            // Cita 4: Cancelada (de ayer)
            Cita citaCancelada = new Cita();
            citaCancelada.setPaciente(paciente);
            citaCancelada.setDoctor(doctor);
            citaCancelada.setSede(sedeCentral);
            citaCancelada.setConsultorio(consultorio1);
            citaCancelada.setFechaHoraInicio(LocalDateTime.of(hoy.minusDays(1), LocalTime.of(12, 0)));
            citaCancelada.setFechaHoraFin(LocalDateTime.of(hoy.minusDays(1), LocalTime.of(12, 30)));
            citaCancelada.setEstado("cancelada");
            citaCancelada.setEstadoPago("pendiente");
            citaCancelada.setPagoAnticipado(false);
            citaCancelada.setOrigen("interno");
            citaCancelada.setCreadoPorUsuario(admin);
            citaRepository.save(citaCancelada);
        }

        log.info("Carga de datos de prueba finalizada exitosamente.");
    }
}
