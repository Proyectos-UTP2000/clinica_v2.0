package com.web.clinica.config;

import com.web.clinica.model.*;
import com.web.clinica.repository.*;
import com.web.clinica.security.PermissionCatalog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataLoader implements CommandLineRunner {

    private final PermisoRepository permisoRepository;
    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final SedeRepository sedeRepository;
    private final EspecialidadRepository especialidadRepository;
    private final ConsultorioRepository consultorioRepository;
    private final DoctorRepository doctorRepository;
    private final SecretariaRepository secretariaRepository;
    private final PacienteRepository pacienteRepository;
    private final DisponibilidadBaseRepository disponibilidadBaseRepository;
    private final ExcepcionDisponibilidadRepository excepcionDisponibilidadRepository;
    private final ConfiguracionGlobalRepository configuracionGlobalRepository;
    private final CitaRepository citaRepository;
    private final ConsultaRepository consultaRepository;
    private final PagoRepository pagoRepository;
    private final CajaDiariaRepository cajaDiariaRepository;
    private final RecetaRepository recetaRepository;
    private final NotaEvolucionRepository notaEvolucionRepository;
    private final IndicacionMedicaRepository indicacionMedicaRepository;
    private final EstudioComplementarioRepository estudioComplementarioRepository;
    private final AuditLogRepository auditLogRepository;
    private final CodigoVerificacionRepository codigoVerificacionRepository;
    private final AdjuntoRepository adjuntoRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        log.info("Iniciando carga de datos iniciales en la base de datos...");

        // 1. Cargar Permisos
        log.info("Cargando catálogo de permisos...");
        List<PermissionCatalog.PermissionDefinition> allPerms = PermissionCatalog.all();
        for (PermissionCatalog.PermissionDefinition def : allPerms) {
            if (!permisoRepository.findByCodigo(def.codigo()).isPresent()) {
                Permiso p = new Permiso();
                p.setCodigo(def.codigo());
                p.setDescripcion(def.descripcion());
                permisoRepository.save(p);
            }
        }

        // Obtener permisos actualizados para las asignaciones
        List<Permiso> permisosDb = permisoRepository.findAll();

        // 2. Cargar Roles y asociar sus permisos
        log.info("Cargando roles y asignando permisos...");

        Rol rolAdmin = rolRepository.findByNombre("Administrador")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("Administrador");
                    r.setDescripcion("Rol administrador con acceso completo al sistema");
                    r.setActivo(true);
                    return r;
                });
        rolAdmin.setPermisos(new HashSet<>(permisosDb)); // Acceso completo
        rolAdmin = rolRepository.save(rolAdmin);

        Rol rolDoctor = rolRepository.findByNombre("Doctor")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("Doctor");
                    r.setDescripcion("Médico con acceso a agenda e historial propio");
                    r.setActivo(true);
                    return r;
                });
        Set<Permiso> doctorPerms = selectPermisos(permisosDb,
                "citas.ver_propias", "historial.ver_propios", "historial.crear",
                "historial.editar", "disponibilidad.ver_propia", "disponibilidad.editar_propia",
                "dashboard.ver", "justificaciones.ver_propias"
        );
        rolDoctor.setPermisos(doctorPerms);
        rolDoctor = rolRepository.save(rolDoctor);

        Rol rolSecretaria = rolRepository.findByNombre("Secretaria")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("Secretaria");
                    r.setDescripcion("Secretaria con gestión operativa de citas y pagos");
                    r.setActivo(true);
                    return r;
                });
        Set<Permiso> secretariaPerms = selectPermisos(permisosDb,
                "pacientes.ver", "pacientes.crear", "pacientes.editar", "medicos.ver",
                "sedes.ver", "especialidades.ver", "citas.ver_todas", "citas.crear",
                "citas.editar_asignados", "citas.cancelar", "pagos.ver", "pagos.crear",
                "dashboard.ver", "caja.gestionar", "caja.ver"
        );
        rolSecretaria.setPermisos(secretariaPerms);
        rolSecretaria = rolRepository.save(rolSecretaria);

        Rol rolEnfermera = rolRepository.findByNombre("Enfermera")
                .orElseGet(() -> {
                    Rol r = new Rol();
                    r.setNombre("Enfermera");
                    r.setDescripcion("Enfermera con acceso de consulta a pacientes e historiales básicos");
                    r.setActivo(true);
                    return r;
                });
        Set<Permiso> enfermeraPerms = selectPermisos(permisosDb,
                "pacientes.ver", "medicos.ver", "citas.ver_todas", "historial.ver_basico", "dashboard.ver"
        );
        rolEnfermera.setPermisos(enfermeraPerms);
        rolEnfermera = rolRepository.save(rolEnfermera);

        // Hash de contraseña por defecto para desarrollo
        String defaultPasswordHash = passwordEncoder.encode("Password123");

        // 3. Cargar Usuarios
        log.info("Cargando usuarios por rol...");

        // Admin
        Usuario adminUser = usuarioRepository.findByEmail("admin@clinica.com")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setDni("00000000");
                    u.setNombres("Admin");
                    u.setApellidos("Sistema");
                    u.setEmail("admin@clinica.com");
                    u.setActivo(true);
                    return u;
                });
        adminUser.setPasswordHash(defaultPasswordHash);
        adminUser.setCambioPasswordObligatorio(false);
        adminUser.setRoles(new HashSet<>(Collections.singletonList(rolAdmin)));
        adminUser = usuarioRepository.save(adminUser);

        // Doctor 1 (Diana)
        Usuario doctorDianaUser = usuarioRepository.findByEmail("diana.medico@clinica.com")
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
        doctorDianaUser.setPasswordHash(defaultPasswordHash);
        doctorDianaUser.setCambioPasswordObligatorio(false);
        doctorDianaUser.setRoles(new HashSet<>(Collections.singletonList(rolDoctor)));
        doctorDianaUser = usuarioRepository.save(doctorDianaUser);

        // Doctor 2 (Pedro)
        Usuario doctorPedroUser = usuarioRepository.findByEmail("pedro.pediatra@clinica.com")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setDni("55555555");
                    u.setNombres("Pedro");
                    u.setApellidos("Pediatra");
                    u.setEmail("pedro.pediatra@clinica.com");
                    u.setTelefono("999555666");
                    u.setFechaNacimiento(LocalDate.of(1980, 5, 25));
                    u.setActivo(true);
                    return u;
                });
        doctorPedroUser.setPasswordHash(defaultPasswordHash);
        doctorPedroUser.setCambioPasswordObligatorio(false);
        doctorPedroUser.setRoles(new HashSet<>(Collections.singletonList(rolDoctor)));
        doctorPedroUser = usuarioRepository.save(doctorPedroUser);

        // Secretaria (Sara)
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
        secUsuario.setRoles(new HashSet<>(Collections.singletonList(rolSecretaria)));
        secUsuario = usuarioRepository.save(secUsuario);

        // Enfermera (Elena)
        Usuario enfUsuario = usuarioRepository.findByEmail("elena.enfermera@clinica.com")
                .orElseGet(() -> {
                    Usuario u = new Usuario();
                    u.setDni("33333333");
                    u.setNombres("Elena");
                    u.setApellidos("Enfermera");
                    u.setEmail("elena.enfermera@clinica.com");
                    u.setTelefono("999333444");
                    u.setFechaNacimiento(LocalDate.of(1992, 11, 05));
                    u.setActivo(true);
                    return u;
                });
        enfUsuario.setPasswordHash(defaultPasswordHash);
        enfUsuario.setCambioPasswordObligatorio(false);
        enfUsuario.setRoles(new HashSet<>(Collections.singletonList(rolEnfermera)));
        enfUsuario = usuarioRepository.save(enfUsuario);

        // 4. Cargar Sedes
        log.info("Cargando sedes...");
        Sede sedeCentral = sedeRepository.findAll().stream()
                .filter(s -> "Sede Central".equalsIgnoreCase(s.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Sede s = new Sede();
                    s.setNombre("Sede Central");
                    s.setDireccion("Av. Salud 123, Lima");
                    s.setActivo(true);
                    return s;
                });
        sedeCentral = sedeRepository.save(sedeCentral);

        Sede SedeMiraflores = sedeRepository.findAll().stream()
                .filter(s -> "Sede Miraflores".equalsIgnoreCase(s.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Sede s = new Sede();
                    s.setNombre("Sede Miraflores");
                    s.setDireccion("Av. Larco 456, Miraflores");
                    s.setActivo(true);
                    return s;
                });
        SedeMiraflores = sedeRepository.save(SedeMiraflores);

        // 5. Cargar Especialidades
        log.info("Cargando especialidades...");
        Especialidad espGeneral = especialidadRepository.findAll().stream()
                .filter(e -> "Medicina General".equalsIgnoreCase(e.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Especialidad e = new Especialidad();
                    e.setNombre("Medicina General");
                    e.setDescripcion("Atención primaria y control general");
                    return e;
                });
        espGeneral = especialidadRepository.save(espGeneral);

        Especialidad espPediatria = especialidadRepository.findAll().stream()
                .filter(e -> "Pediatría".equalsIgnoreCase(e.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Especialidad e = new Especialidad();
                    e.setNombre("Pediatría");
                    e.setDescripcion("Atención médica para niños y adolescentes");
                    return e;
                });
        espPediatria = especialidadRepository.save(espPediatria);

        Especialidad espCardiologia = especialidadRepository.findAll().stream()
                .filter(e -> "Cardiología".equalsIgnoreCase(e.getNombre()))
                .findFirst()
                .orElseGet(() -> {
                    Especialidad e = new Especialidad();
                    e.setNombre("Cardiología");
                    e.setDescripcion("Salud y control cardiovascular");
                    return e;
                });
        espCardiologia = especialidadRepository.save(espCardiologia);

        // Asociar Especialidades a Sedes
        log.info("Asociando especialidades a sedes...");
        if (sedeCentral.getEspecialidades() == null || sedeCentral.getEspecialidades().isEmpty()) {
            Set<Especialidad> centralEsps = new HashSet<>();
            centralEsps.add(espGeneral);
            centralEsps.add(espPediatria);
            centralEsps.add(espCardiologia);
            sedeCentral.setEspecialidades(centralEsps);
            sedeCentral = sedeRepository.save(sedeCentral);
        }

        if (SedeMiraflores.getEspecialidades() == null || SedeMiraflores.getEspecialidades().isEmpty()) {
            Set<Especialidad> mirafloresEsps = new HashSet<>();
            mirafloresEsps.add(espGeneral);
            mirafloresEsps.add(espPediatria);
            SedeMiraflores.setEspecialidades(mirafloresEsps);
            SedeMiraflores = sedeRepository.save(SedeMiraflores);
        }

        // 6. Cargar Consultorios
        log.info("Cargando consultorios...");
        final Sede finalSedeCentral = sedeCentral;
        Consultorio consultorio101 = consultorioRepository.findBySedeIdAndNombreIgnoreCase(sedeCentral.getId(), "Consultorio 101")
                .orElseGet(() -> {
                    Consultorio c = Consultorio.builder()
                            .sede(finalSedeCentral)
                            .nombre("Consultorio 101")
                            .piso("1")
                            .area("Medicina General")
                            .activo(true)
                            .build();
                    return consultorioRepository.save(c);
                });

        Consultorio consultorio102 = consultorioRepository.findBySedeIdAndNombreIgnoreCase(sedeCentral.getId(), "Consultorio 102")
                .orElseGet(() -> {
                    Consultorio c = Consultorio.builder()
                            .sede(finalSedeCentral)
                            .nombre("Consultorio 102")
                            .piso("1")
                            .area("Pediatría")
                            .activo(true)
                            .build();
                    return consultorioRepository.save(c);
                });

        Consultorio consultorio201 = consultorioRepository.findBySedeIdAndNombreIgnoreCase(sedeCentral.getId(), "Consultorio 201")
                .orElseGet(() -> {
                    Consultorio c = Consultorio.builder()
                            .sede(finalSedeCentral)
                            .nombre("Consultorio 201")
                            .piso("2")
                            .area("Cardiología")
                            .activo(true)
                            .build();
                    return consultorioRepository.save(c);
                });

        final Sede finalSedeMiraflores = SedeMiraflores;
        Consultorio consultorioMiraflores101 = consultorioRepository.findBySedeIdAndNombreIgnoreCase(SedeMiraflores.getId(), "Consultorio A-101")
                .orElseGet(() -> {
                    Consultorio c = Consultorio.builder()
                            .sede(finalSedeMiraflores)
                            .nombre("Consultorio A-101")
                            .piso("1")
                            .area("Medicina General")
                            .activo(true)
                            .build();
                    return consultorioRepository.save(c);
                });

        // 7. Cargar Doctores
        log.info("Cargando doctores y asociando consultorios y sedes...");
        final Usuario finalDoctorDianaUser = doctorDianaUser;
        final Especialidad finalEspGeneral = espGeneral;
        final Sede finalSedeCentralSaved = sedeCentral;
        final Sede finalSedeMirafloresSaved = SedeMiraflores;
        final Consultorio finalConsultorio101 = consultorio101;
        final Consultorio finalConsultorioMiraflores101 = consultorioMiraflores101;
        Doctor drDiana = doctorRepository.findByUsuarioDni(doctorDianaUser.getDni())
                .orElseGet(() -> {
                    Doctor d = new Doctor();
                    d.setUsuario(finalDoctorDianaUser);
                    d.setEspecialidad(finalEspGeneral);
                    d.setSedes(new HashSet<>(List.of(finalSedeCentralSaved, finalSedeMirafloresSaved)));
                    d.setConsultorios(new HashSet<>(List.of(finalConsultorio101, finalConsultorioMiraflores101)));
                    return doctorRepository.save(d);
                });

        final Usuario finalDoctorPedroUser = doctorPedroUser;
        final Especialidad finalEspPediatria = espPediatria;
        final Consultorio finalConsultorio102 = consultorio102;
        Doctor drPedro = doctorRepository.findByUsuarioDni(doctorPedroUser.getDni())
                .orElseGet(() -> {
                    Doctor d = new Doctor();
                    d.setUsuario(finalDoctorPedroUser);
                    d.setEspecialidad(finalEspPediatria);
                    d.setSedes(new HashSet<>(List.of(finalSedeCentralSaved, finalSedeMirafloresSaved)));
                    d.setConsultorios(new HashSet<>(List.of(finalConsultorio102)));
                    return doctorRepository.save(d);
                });

        // 8. Cargar Secretarias
        log.info("Cargando secretarias...");
        final Doctor doctorDianaRef = drDiana;
        final Doctor doctorPedroRef = drPedro;
        final Usuario finalSecUsuario = secUsuario;
        Secretaria secretariaSara = secretariaRepository.findByUsuarioId(secUsuario.getId())
                .orElseGet(() -> {
                    Secretaria s = new Secretaria();
                    s.setUsuario(finalSecUsuario);
                    s.setDoctores(new HashSet<>(List.of(doctorDianaRef, doctorPedroRef)));
                    return secretariaRepository.save(s);
                });

        // 9. Cargar Disponibilidad Base para los Médicos (Lunes a Viernes)
        log.info("Cargando disponibilidad base para médicos...");
        if (disponibilidadBaseRepository.findByDoctorIdOrderByDiaSemanaAscHoraInicioAsc(drDiana.getId()).isEmpty()) {
            for (int i = 1; i <= 5; i++) {
                DisponibilidadBase db = new DisponibilidadBase();
                db.setDoctor(drDiana);
                db.setSede(finalSedeCentralSaved);
                db.setDiaSemana(i);
                db.setHoraInicio(LocalTime.of(8, 0));
                db.setHoraFin(LocalTime.of(13, 0));
                db.setConsultorio(finalConsultorio101);
                disponibilidadBaseRepository.save(db);
            }
        }

        if (disponibilidadBaseRepository.findByDoctorIdOrderByDiaSemanaAscHoraInicioAsc(drPedro.getId()).isEmpty()) {
            for (int i = 1; i <= 5; i++) {
                DisponibilidadBase db = new DisponibilidadBase();
                db.setDoctor(drPedro);
                db.setSede(finalSedeCentralSaved);
                db.setDiaSemana(i);
                db.setHoraInicio(LocalTime.of(14, 0));
                db.setHoraFin(LocalTime.of(19, 0));
                db.setConsultorio(finalConsultorio102);
                disponibilidadBaseRepository.save(db);
            }
        }

        // 10. Cargar Excepciones de Disponibilidad
        log.info("Cargando excepciones de disponibilidad...");
        LocalDate manana = LocalDate.now().plusDays(1);
        final Doctor finalDrDiana = drDiana;
        List<ExcepcionDisponibilidad> excepcionesExistentes = excepcionDisponibilidadRepository.findByDoctorAndFecha(drDiana, manana);
        if (excepcionesExistentes.isEmpty()) {
            ExcepcionDisponibilidad exc = new ExcepcionDisponibilidad();
            exc.setDoctor(finalDrDiana);
            exc.setFecha(manana);
            exc.setHoraInicio(LocalTime.of(10, 0));
            exc.setHoraFin(LocalTime.of(11, 30));
            exc.setMotivo("Capacitación Médica Interna");
            excepcionDisponibilidadRepository.save(exc);
        }

        // 11. Cargar Pacientes de prueba
        log.info("Cargando pacientes de prueba...");
        Paciente pacientePablo = pacienteRepository.findByDni("44444444")
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
        pacientePablo.setPasswordHash(defaultPasswordHash);
        pacienteRepository.save(pacientePablo);

        Paciente pacienteMaria = pacienteRepository.findByDni("77777777")
                .orElseGet(() -> {
                    Paciente p = new Paciente();
                    p.setDni("77777777");
                    p.setNombres("María");
                    p.setApellidos("Paciente");
                    p.setSexo("F");
                    p.setFechaNacimiento(LocalDate.of(1990, 5, 15));
                    p.setTelefono("977666555");
                    p.setEmail("maria.paciente@example.com");
                    p.setActivo(true);
                    return p;
                });
        pacienteMaria.setPasswordHash(defaultPasswordHash);
        pacienteRepository.save(pacienteMaria);

        // 12. Cargar Configuración Global
        log.info("Cargando configuraciones globales de la clínica...");
        String[][] configs = {
                {"clinica.nombre", "Clínica San Lucas"},
                {"clinica.razon_social", "San Lucas S.A.C."},
                {"clinica.direccion", "Av. Larco 123, Miraflores, Lima"},
                {"clinica.telefono", "01 444-5555"},
                {"clinica.logotipo", ""}
        };
        for (String[] configPair : configs) {
            String clave = configPair[0];
            String valor = configPair[1];
            if (!configuracionGlobalRepository.findById(clave).isPresent()) {
                ConfiguracionGlobal conf = ConfiguracionGlobal.builder()
                        .clave(clave)
                        .valor(valor)
                        .build();
                configuracionGlobalRepository.save(conf);
            }
        }        // 13. Cargar Caja Diaria
        log.info("Cargando caja diaria...");
        CajaDiaria cajaAyer = null;
        CajaDiaria cajaHoy = null;

        if (cajaDiariaRepository.count() == 0) {
            LocalDate ayer = LocalDate.now().minusDays(1);
            cajaAyer = CajaDiaria.builder()
                    .fecha(ayer)
                    .montoApertura(new BigDecimal("100.00"))
                    .montoCierre(new BigDecimal("250.00"))
                    .ingresos(new BigDecimal("150.00"))
                    .egresos(BigDecimal.ZERO)
                    .balanceReal(new BigDecimal("250.00"))
                    .diferencia(BigDecimal.ZERO)
                    .estado("cerrada")
                    .fechaApertura(ayer.atTime(8, 0))
                    .fechaCierre(ayer.atTime(18, 0))
                    .abiertoPorUsuario(adminUser)
                    .cerradoPorUsuario(adminUser)
                    .observaciones("Cierre de caja de ayer sin novedades")
                    .build();
            cajaAyer = cajaDiariaRepository.save(cajaAyer);

            LocalDate hoy = LocalDate.now();
            cajaHoy = CajaDiaria.builder()
                    .fecha(hoy)
                    .montoApertura(new BigDecimal("250.00"))
                    .ingresos(BigDecimal.ZERO) // Se actualizará al asociar pagos
                    .egresos(BigDecimal.ZERO)
                    .estado("abierta")
                    .fechaApertura(hoy.atTime(8, 0))
                    .abiertoPorUsuario(adminUser)
                    .observaciones("Caja de hoy abierta")
                    .build();
            cajaHoy = cajaDiariaRepository.save(cajaHoy);
        } else {
            List<CajaDiaria> cajas = cajaDiariaRepository.findAll();
            for (CajaDiaria c : cajas) {
                if (c.getFecha().equals(LocalDate.now().minusDays(1))) {
                    cajaAyer = c;
                } else if (c.getFecha().equals(LocalDate.now())) {
                    cajaHoy = c;
                }
            }
        }

        // 14. Cargar Citas
        log.info("Cargando citas de prueba...");
        Cita citaAyerAtendida = null;
        Cita citaHoyConfirmada = null;
        Cita citaHoyPendiente = null;

        if (citaRepository.count() == 0) {
            LocalDate ayer = LocalDate.now().minusDays(1);
            LocalDate hoy = LocalDate.now();

            // Cita de ayer (atendida)
            citaAyerAtendida = new Cita();
            citaAyerAtendida.setPaciente(pacientePablo);
            citaAyerAtendida.setDoctor(drDiana);
            citaAyerAtendida.setSede(sedeCentral);
            citaAyerAtendida.setConsultorio(consultorio101);
            citaAyerAtendida.setFechaHoraInicio(ayer.atTime(9, 0));
            citaAyerAtendida.setFechaHoraFin(ayer.atTime(9, 30));
            citaAyerAtendida.setEstado("atendida");
            citaAyerAtendida.setEstadoPago("pagado");
            citaAyerAtendida.setPagoAnticipado(true);
            citaAyerAtendida.setReprogramacionesRestantes(2);
            citaAyerAtendida.setOrigen("web");
            citaAyerAtendida.setCreadoPorUsuario(adminUser);
            citaAyerAtendida = citaRepository.save(citaAyerAtendida);

            // Cita de ayer (cancelada)
            Cita citaAyerCancelada = new Cita();
            citaAyerCancelada.setPaciente(pacienteMaria);
            citaAyerCancelada.setDoctor(drPedro);
            citaAyerCancelada.setSede(sedeCentral);
            citaAyerCancelada.setConsultorio(consultorio102);
            citaAyerCancelada.setFechaHoraInicio(ayer.atTime(15, 0));
            citaAyerCancelada.setFechaHoraFin(ayer.atTime(15, 30));
            citaAyerCancelada.setEstado("cancelada");
            citaAyerCancelada.setEstadoPago("pendiente");
            citaAyerCancelada.setPagoAnticipado(false);
            citaAyerCancelada.setReprogramacionesRestantes(2);
            citaAyerCancelada.setOrigen("interno");
            citaAyerCancelada.setCreadoPorUsuario(adminUser);
            citaRepository.save(citaAyerCancelada);

            // Cita de hoy (confirmada)
            citaHoyConfirmada = new Cita();
            citaHoyConfirmada.setPaciente(pacientePablo);
            citaHoyConfirmada.setDoctor(drDiana);
            citaHoyConfirmada.setSede(sedeCentral);
            citaHoyConfirmada.setConsultorio(consultorio101);
            citaHoyConfirmada.setFechaHoraInicio(hoy.atTime(10, 0));
            citaHoyConfirmada.setFechaHoraFin(hoy.atTime(10, 30));
            citaHoyConfirmada.setEstado("confirmada");
            citaHoyConfirmada.setEstadoPago("pagado");
            citaHoyConfirmada.setPagoAnticipado(true);
            citaHoyConfirmada.setReprogramacionesRestantes(2);
            citaHoyConfirmada.setOrigen("web");
            citaHoyConfirmada.setCreadoPorUsuario(adminUser);
            citaHoyConfirmada = citaRepository.save(citaHoyConfirmada);

            // Cita de hoy (pendiente)
            citaHoyPendiente = new Cita();
            citaHoyPendiente.setPaciente(pacienteMaria);
            citaHoyPendiente.setDoctor(drPedro);
            citaHoyPendiente.setSede(sedeCentral);
            citaHoyPendiente.setConsultorio(consultorio102);
            citaHoyPendiente.setFechaHoraInicio(hoy.atTime(16, 0));
            citaHoyPendiente.setFechaHoraFin(hoy.atTime(16, 30));
            citaHoyPendiente.setEstado("programada");
            citaHoyPendiente.setEstadoPago("pendiente");
            citaHoyPendiente.setPagoAnticipado(false);
            citaHoyPendiente.setReprogramacionesRestantes(2);
            citaHoyPendiente.setOrigen("interno");
            citaHoyPendiente.setCreadoPorUsuario(adminUser);
            citaHoyPendiente = citaRepository.save(citaHoyPendiente);

            // Cita de mañana (confirmada)
            Cita citaManana = new Cita();
            citaManana.setPaciente(pacientePablo);
            citaManana.setDoctor(drPedro);
            citaManana.setSede(sedeCentral);
            citaManana.setConsultorio(consultorio102);
            citaManana.setFechaHoraInicio(manana.atTime(14, 0));
            citaManana.setFechaHoraFin(manana.atTime(14, 30));
            citaManana.setEstado("confirmada");
            citaManana.setEstadoPago("pendiente");
            citaManana.setPagoAnticipado(false);
            citaManana.setReprogramacionesRestantes(2);
            citaManana.setOrigen("web");
            citaManana.setCreadoPorUsuario(adminUser);
            citaRepository.save(citaManana);
        }

        // 15. Cargar Pagos
        log.info("Cargando pagos de prueba...");
        if (pagoRepository.count() == 0) {
            if (citaAyerAtendida != null && cajaAyer != null) {
                Pago pagoAyer = Pago.builder()
                        .cita(citaAyerAtendida)
                        .monto(new BigDecimal("150.00"))
                        .metodo("efectivo")
                        .fechaPago(LocalDate.now().minusDays(1).atTime(8, 55))
                        .registradoPorUsuario(adminUser)
                        .cajaDiaria(cajaAyer)
                        .build();
                pagoRepository.save(pagoAyer);
            }

            if (citaHoyConfirmada != null && cajaHoy != null) {
                BigDecimal montoPago = new BigDecimal("120.00");
                Pago pagoHoy = Pago.builder()
                        .cita(citaHoyConfirmada)
                        .monto(montoPago)
                        .metodo("tarjeta")
                        .fechaPago(LocalDate.now().atTime(9, 30))
                        .registradoPorUsuario(adminUser)
                        .cajaDiaria(cajaHoy)
                        .build();
                pagoRepository.save(pagoHoy);

                // Actualizar ingresos de caja de hoy
                cajaHoy.setIngresos(montoPago);
                cajaDiariaRepository.save(cajaHoy);
            }
        }

        // 16. Cargar Consultas
        log.info("Cargando consultas de prueba...");
        Consulta consultaAyer = null;
        if (consultaRepository.count() == 0 && citaAyerAtendida != null) {
            consultaAyer = Consulta.builder()
                    .paciente(pacientePablo)
                    .doctor(drDiana)
                    .cita(citaAyerAtendida)
                    .sede(sedeCentral)
                    .fechaHora(LocalDate.now().minusDays(1).atTime(9, 10))
                    .tipo("control")
                    .motivoConsulta("Dolor de cabeza continuo, cansancio visual y fatiga generalizada.")
                    .diagnostico("Cefalea tensional asociada a estrés laboral y fatiga ocular.")
                    .observaciones("Se indica descanso médico por 24 horas. Evitar pantallas electrónicas. Control en 15 días.")
                    .estado("finalizada")
                    .build();
            consultaAyer = consultaRepository.save(consultaAyer);
        }

        // 17. Cargar Recetas
        log.info("Cargando recetas de prueba...");
        if (recetaRepository.count() == 0 && consultaAyer != null) {
            Receta receta1 = Receta.builder()
                    .consulta(consultaAyer)
                    .medicamento("Paracetamol 500mg")
                    .dosis("1 tableta")
                    .frecuencia("Cada 8 horas")
                    .duracion("3 días")
                    .indicaciones("Tomar preferentemente con alimentos. No exceder dosis recomendada.")
                    .build();
            recetaRepository.save(receta1);

            Receta receta2 = Receta.builder()
                    .consulta(consultaAyer)
                    .medicamento("Ibuprofeno 400mg")
                    .dosis("1 tableta")
                    .frecuencia("Cada 12 horas (si persiste dolor intenso)")
                    .duracion("3 días")
                    .indicaciones("Tomar junto con protector gástrico o con comida abundante.")
                    .build();
            recetaRepository.save(receta2);
        }

        // 18. Cargar Notas de Evolución
        log.info("Cargando notas de evolución...");
        if (notaEvolucionRepository.count() == 0 && consultaAyer != null) {
            NotaEvolucion nota = NotaEvolucion.builder()
                    .consulta(consultaAyer)
                    .fecha(LocalDate.now().minusDays(1).atTime(9, 25))
                    .nota("El paciente responde favorablemente a la presión digital leve en las sienes. Presión arterial registrada de 125/80 mmHg. Frecuencia cardíaca de 72 lpm. Sin signos de alerta neurológica.")
                    .autor(doctorDianaUser)
                    .build();
            notaEvolucionRepository.save(nota);
        }

        // 19. Cargar Indicaciones Médicas
        log.info("Cargando indicaciones médicas...");
        if (indicacionMedicaRepository.count() == 0 && consultaAyer != null) {
            IndicacionMedica ind1 = IndicacionMedica.builder()
                    .consulta(consultaAyer)
                    .tipo("reposo")
                    .descripcion("Realizar pausas activas oculares de 5 minutos por cada hora de trabajo frente a pantallas.")
                    .build();
            indicacionMedicaRepository.save(ind1);

            IndicacionMedica ind2 = IndicacionMedica.builder()
                    .consulta(consultaAyer)
                    .tipo("reposo")
                    .descripcion("Mantener una correcta hidratación (mínimo 2 litros de agua diarios) y reducir consumo de cafeína por las tardes.")
                    .build();
            indicacionMedicaRepository.save(ind2);
        }

        // 20. Cargar Estudios Complementarios
        log.info("Cargando estudios complementarios...");
        if (estudioComplementarioRepository.count() == 0 && consultaAyer != null) {
            EstudioComplementario estudio = EstudioComplementario.builder()
                    .consulta(consultaAyer)
                    .tipoEstudio("Examen Oftalmológico Completo")
                    .detalle("Descartar errores de refracción o necesidad de lentes de descanso.")
                    .estado("pendiente")
                    .build();
            estudioComplementarioRepository.save(estudio);
        }

        // 21. Cargar Adjuntos
        log.info("Cargando adjuntos de prueba...");
        if (adjuntoRepository.count() == 0 && consultaAyer != null) {
            Adjunto adj = Adjunto.builder()
                    .consulta(consultaAyer)
                    .nombreArchivo("anamnesis_inicial.pdf")
                    .ruta("/uploads/consultas/anamnesis_inicial.pdf")
                    .tipoMime("application/pdf")
                    .fechaSubida(LocalDate.now().minusDays(1).atTime(9, 15))
                    .build();
            adjuntoRepository.save(adj);
        }

        // 22. Cargar Códigos de Verificación
        log.info("Cargando códigos de verificación...");
        if (codigoVerificacionRepository.count() == 0) {
            CodigoVerificacion cod = new CodigoVerificacion();
            cod.setEmail("pablo.paciente@example.com");
            cod.setCodigo("987654");
            cod.setTipo("verificacion_registro");
            cod.setUsado(true);
            cod.setFechaExpiracion(LocalDateTime.now().minusHours(1));
            codigoVerificacionRepository.save(cod);

            CodigoVerificacion cod2 = new CodigoVerificacion();
            cod2.setEmail("maria.paciente@example.com");
            cod2.setCodigo("123456");
            cod2.setTipo("recuperacion");
            cod2.setUsado(false);
            cod2.setFechaExpiracion(LocalDateTime.now().plusHours(25));
            codigoVerificacionRepository.save(cod2);
        }

        // 23. Cargar Logs de Auditoría
        log.info("Cargando logs de auditoría...");
        if (auditLogRepository.count() == 0) {
            AuditLog log1 = AuditLog.builder()
                    .usuarioDni("00000000")
                    .usuarioNombre("Admin Sistema")
                    .accion("INICIO_SESION")
                    .detalles("El usuario inició sesión en la aplicación web.")
                    .ipAddress("127.0.0.1")
                    .estado("EXITOSO")
                    .fecha(LocalDate.now().minusDays(1).atTime(8, 0))
                    .build();
            auditLogRepository.save(log1);

            AuditLog log2 = AuditLog.builder()
                    .usuarioDni("22222222")
                    .usuarioNombre("Sara Secretaria")
                    .accion("CREAR_CITA")
                    .detalles("Se registró una nueva cita para el paciente Pablo Paciente con el Doctor Diana.")
                    .ipAddress("192.168.1.15")
                    .estado("EXITOSO")
                    .fecha(LocalDate.now().minusDays(1).atTime(8, 30))
                    .build();
            auditLogRepository.save(log2);

            AuditLog log3 = AuditLog.builder()
                    .usuarioDni("11111111")
                    .usuarioNombre("Diana Medico")
                    .accion("ATENDER_CITA")
                    .detalles("El doctor inició la atención y registro de consulta del paciente Pablo Paciente.")
                    .ipAddress("192.168.1.20")
                    .estado("EXITOSO")
                    .fecha(LocalDate.now().minusDays(1).atTime(9, 5))
                    .build();
            auditLogRepository.save(log3);
        }

        log.info("Carga de datos iniciales finalizada exitosamente.");
    }

    private Set<Permiso> selectPermisos(List<Permiso> source, String... codigos) {
        Set<String> targetCodes = Set.of(codigos);
        return source.stream()
                .filter(p -> targetCodes.contains(p.getCodigo()))
                .collect(Collectors.toSet());
    }
}
