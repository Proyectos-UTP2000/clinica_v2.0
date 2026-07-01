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

import java.time.DayOfWeek;
import java.time.LocalDate;
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
