package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.CitaCreateRequest;
import com.web.clinica.dto.response.CitaResponse;
import com.web.clinica.dto.response.DisponibilidadSlotResponse;
import com.web.clinica.exception.AccesoDenegadoException;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Cita;
import com.web.clinica.model.DisponibilidadBase;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.ExcepcionDisponibilidad;
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
import com.web.clinica.service.abstractService.ICitaService;
import com.web.clinica.util.EmailService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CitaServiceImpl implements ICitaService {

    private static final int DURACION_CITA_MINUTOS = 30;
    private static final String ESTADO_CANCELADA = "cancelada";
    private static final String ESTADO_PROGRAMADA = "programada";
    private static final String ESTADO_REPROGRAMADA = "reprogramada";

    private final CitaRepository citaRepository;
    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final SedeRepository sedeRepository;
    private final DisponibilidadBaseRepository disponibilidadBaseRepository;
    private final ExcepcionDisponibilidadRepository excepcionDisponibilidadRepository;
    private final SecretariaRepository secretariaRepository;
    private final EmailService emailService;

    /** Agenda una cita interna validando disponibilidad y conflictos. */
    @Override
    @Transactional
    public CitaResponse crear(CitaCreateRequest solicitud) {
        Paciente paciente = obtenerPaciente(solicitud.getPacienteId());
        Doctor doctor = obtenerDoctor(solicitud.getDoctorId());
        Sede sede = obtenerSede(solicitud.getSedeId());
        LocalDateTime fechaHoraFin = solicitud.getFechaHoraInicio().plusMinutes(DURACION_CITA_MINUTOS);

        validarDoctorAtiendeEnSede(doctor, sede);
        validarDisponibilidad(doctor, sede, solicitud.getFechaHoraInicio(), fechaHoraFin, null);
        validarPacienteSinCruce(paciente, solicitud.getFechaHoraInicio(), fechaHoraFin, null);

        Cita cita = new Cita();
        cita.setPaciente(paciente);
        cita.setDoctor(doctor);
        cita.setSede(sede);
        cita.setFechaHoraInicio(solicitud.getFechaHoraInicio());
        cita.setFechaHoraFin(fechaHoraFin);
        cita.setEstado(ESTADO_PROGRAMADA);
        cita.setEstadoPago("pendiente");
        cita.setReprogramacionesRestantes(2);
        cita.setOrigen("interno");
        cita.setCreadoPorUsuario(obtenerUsuarioActual());

        Cita citaGuardada = citaRepository.save(cita);
        emailService.enviarCorreo(
                paciente.getEmail(),
                "Cita programada",
                "Su cita fue programada para " + citaGuardada.getFechaHoraInicio()
        );
        return convertirRespuesta(citaGuardada);
    }

    /** Reprograma la cita y consume un intento disponible. */
    @Override
    @Transactional
    public CitaResponse reprogramar(Long citaId, LocalDateTime nuevaFechaHora) {
        Cita cita = obtenerEntidad(citaId);
        verificarPermisoEdicionCita(cita);
        if (ESTADO_CANCELADA.equals(cita.getEstado()) || "atendida".equals(cita.getEstado())) {
            throw new BadRequestException("La cita no puede reprogramarse en su estado actual");
        }
        if (cita.getReprogramacionesRestantes() == null || cita.getReprogramacionesRestantes() <= 0) {
            throw new BadRequestException("La cita ya no tiene reprogramaciones disponibles");
        }

        LocalDateTime nuevaFechaHoraFin = nuevaFechaHora.plusMinutes(DURACION_CITA_MINUTOS);
        validarDisponibilidad(cita.getDoctor(), cita.getSede(), nuevaFechaHora, nuevaFechaHoraFin, cita.getId());
        validarPacienteSinCruce(cita.getPaciente(), nuevaFechaHora, nuevaFechaHoraFin, cita.getId());

        cita.setFechaHoraInicio(nuevaFechaHora);
        cita.setFechaHoraFin(nuevaFechaHoraFin);
        cita.setEstado(ESTADO_REPROGRAMADA);
        cita.setReprogramacionesRestantes(cita.getReprogramacionesRestantes() - 1);
        return convertirRespuesta(citaRepository.save(cita));
    }

    /** Cancela una cita despues de validar alcance de edicion. */
    @Override
    @Transactional
    public void cancelar(Long citaId) {
        Cita cita = obtenerEntidad(citaId);
        verificarPermisoEdicionCita(cita);
        cita.setEstado(ESTADO_CANCELADA);
        citaRepository.save(cita);
    }

    /** Obtiene una cita por id. */
    @Override
    @Transactional(readOnly = true)
    public CitaResponse obtenerPorId(Long id) {
        Cita cita = obtenerEntidad(id);
        verificarPermisoVerCita(cita);
        return convertirRespuesta(cita);
    }

    /** Lista citas con filtros opcionales para vista interna. */
    @Override
    @Transactional(readOnly = true)
    public Page<CitaResponse> listarConFiltros(Long pacienteId,
                                               Long doctorId,
                                               Long sedeId,
                                               LocalDate fecha,
                                               LocalDate fechaInicio,
                                               LocalDate fechaFin,
                                               Pageable pageable) {
        LocalDateTime inicio = resolverInicio(fecha, fechaInicio);
        LocalDateTime fin = resolverFin(fecha, fechaInicio, fechaFin);
        if (inicio != null && fin != null && fin.isBefore(inicio)) {
            throw new BadRequestException("La fecha final no puede ser anterior a la inicial");
        }

        Specification<Cita> specification = Specification.where(null);
        if (pacienteId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("paciente").get("id"), pacienteId));
        }
        if (doctorId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("doctor").get("id"), doctorId));
        }
        if (sedeId != null) {
            specification = specification.and((root, query, builder) -> builder.equal(root.get("sede").get("id"), sedeId));
        }
        if (inicio != null) {
            specification = specification.and((root, query, builder) -> builder.greaterThanOrEqualTo(root.get("fechaHoraInicio"), inicio));
        }
        if (fin != null) {
            specification = specification.and((root, query, builder) -> builder.lessThan(root.get("fechaHoraInicio"), fin));
        }
        return citaRepository.findAll(specification, pageable).map(this::convertirRespuesta);
    }

    /** Lista citas por paciente. */
    @Override
    @Transactional(readOnly = true)
    public Page<CitaResponse> listarPorPaciente(Long pacienteId, Pageable pageable) {
        return citaRepository.findByPacienteId(pacienteId, pageable).map(this::convertirRespuesta);
    }

    /** Lista citas por doctor y fecha opcional. */
    @Override
    @Transactional(readOnly = true)
    public Page<CitaResponse> listarPorDoctor(Long doctorId,
                                              Pageable pageable,
                                              LocalDate fecha,
                                              LocalDate fechaInicio,
                                              LocalDate fechaFin) {
        return listarConFiltros(null, doctorId, null, fecha, fechaInicio, fechaFin, pageable);
    }

    /** Lista citas propias del doctor autenticado. */
    @Override
    @Transactional(readOnly = true)
    public Page<CitaResponse> listarMisCitas(Pageable pageable, LocalDate fecha, LocalDate fechaInicio, LocalDate fechaFin) {
        Usuario usuario = obtenerUsuarioActual();
        Doctor doctor = doctorRepository.findByUsuarioDni(usuario.getDni())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor autenticado no encontrado"));
        return listarPorDoctor(doctor.getId(), pageable, fecha, fechaInicio, fechaFin);
    }

    /** Calcula slots libres descontando citas y excepciones. */
    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadSlotResponse> obtenerSlotsDisponibles(Long doctorId, Long sedeId, LocalDate fecha) {
        Doctor doctor = obtenerDoctor(doctorId);
        Sede sede = obtenerSede(sedeId);
        validarDoctorAtiendeEnSede(doctor, sede);

        int diaSemana = fecha.getDayOfWeek().getValue();
        List<DisponibilidadBase> horarios = disponibilidadBaseRepository.findByDoctorAndSedeAndDiaSemana(doctor, sede, diaSemana);
        List<DisponibilidadSlotResponse> slots = new ArrayList<>();
        for (DisponibilidadBase horario : horarios) {
            LocalDateTime inicio = fecha.atTime(horario.getHoraInicio());
            LocalDateTime fin = fecha.atTime(horario.getHoraFin());
            while (!inicio.plusMinutes(DURACION_CITA_MINUTOS).isAfter(fin)) {
                LocalDateTime finSlot = inicio.plusMinutes(DURACION_CITA_MINUTOS);
                if (!tieneCruceDoctor(doctor, inicio, finSlot, null)
                        && !tieneCruceExcepcion(doctor, fecha, inicio.toLocalTime(), finSlot.toLocalTime())) {
                    slots.add(new DisponibilidadSlotResponse(inicio, finSlot));
                }
                inicio = finSlot;
            }
        }
        return slots;
    }

    /** Valida horario base, excepciones y citas del doctor. */
    private void validarDisponibilidad(Doctor doctor, Sede sede, LocalDateTime inicio, LocalDateTime fin, Long citaIgnoradaId) {
        LocalDate fecha = inicio.toLocalDate();
        int diaSemana = fecha.getDayOfWeek().getValue();
        boolean dentroHorario = disponibilidadBaseRepository.findByDoctorAndSedeAndDiaSemana(doctor, sede, diaSemana).stream()
                .anyMatch(horario -> !inicio.toLocalTime().isBefore(horario.getHoraInicio())
                        && !fin.toLocalTime().isAfter(horario.getHoraFin()));
        if (!dentroHorario) {
            throw new BadRequestException("El doctor no tiene disponibilidad base para ese horario");
        }
        if (tieneCruceExcepcion(doctor, fecha, inicio.toLocalTime(), fin.toLocalTime())) {
            throw new BadRequestException("El horario se cruza con una excepcion de disponibilidad");
        }
        if (tieneCruceDoctor(doctor, inicio, fin, citaIgnoradaId)) {
            throw new BadRequestException("El doctor ya tiene una cita en ese horario");
        }
    }

    /** Valida que el paciente no tenga doble reserva. */
    private void validarPacienteSinCruce(Paciente paciente, LocalDateTime inicio, LocalDateTime fin, Long citaIgnoradaId) {
        boolean tieneCruce = citaRepository.findByPacienteAndFechaHoraInicioBetween(
                        paciente,
                        inicio.toLocalDate().atStartOfDay(),
                        inicio.toLocalDate().plusDays(1).atStartOfDay()
                ).stream()
                .filter(cita -> !ESTADO_CANCELADA.equals(cita.getEstado()))
                .filter(cita -> citaIgnoradaId == null || !cita.getId().equals(citaIgnoradaId))
                .anyMatch(cita -> haySolapamiento(inicio, fin, cita.getFechaHoraInicio(), cita.getFechaHoraFin()));
        if (tieneCruce) {
            throw new BadRequestException("El paciente ya tiene una cita en ese horario");
        }
    }

    /** Detecta cruce con citas vigentes del doctor. */
    private boolean tieneCruceDoctor(Doctor doctor, LocalDateTime inicio, LocalDateTime fin, Long citaIgnoradaId) {
        return citaRepository.findByDoctorAndFechaHoraInicioBetween(
                        doctor,
                        inicio.toLocalDate().atStartOfDay(),
                        inicio.toLocalDate().plusDays(1).atStartOfDay()
                ).stream()
                .filter(cita -> !ESTADO_CANCELADA.equals(cita.getEstado()))
                .filter(cita -> citaIgnoradaId == null || !cita.getId().equals(citaIgnoradaId))
                .anyMatch(cita -> haySolapamiento(inicio, fin, cita.getFechaHoraInicio(), cita.getFechaHoraFin()));
    }

    /** Detecta cruce con excepciones de disponibilidad. */
    private boolean tieneCruceExcepcion(Doctor doctor, LocalDate fecha, LocalTime inicio, LocalTime fin) {
        return excepcionDisponibilidadRepository.findByDoctorAndFecha(doctor, fecha).stream()
                .anyMatch(excepcion -> haySolapamiento(inicio, fin, excepcion.getHoraInicio(), excepcion.getHoraFin()));
    }

    /** Verifica si dos rangos de fecha-hora se solapan. */
    private boolean haySolapamiento(LocalDateTime inicioA, LocalDateTime finA, LocalDateTime inicioB, LocalDateTime finB) {
        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }

    /** Verifica si dos rangos de hora se solapan. */
    private boolean haySolapamiento(LocalTime inicioA, LocalTime finA, LocalTime inicioB, LocalTime finB) {
        return inicioA.isBefore(finB) && finA.isAfter(inicioB);
    }

    /** Resuelve inicio de filtro para busquedas por dia o rango. */
    private LocalDateTime resolverInicio(LocalDate fecha, LocalDate fechaInicio) {
        if (fecha != null) {
            return fecha.atStartOfDay();
        }
        return fechaInicio == null ? null : fechaInicio.atStartOfDay();
    }

    /** Resuelve fin exclusivo de filtro para busquedas por dia o rango. */
    private LocalDateTime resolverFin(LocalDate fecha, LocalDate fechaInicio, LocalDate fechaFin) {
        if (fecha != null) {
            return fecha.plusDays(1).atStartOfDay();
        }
        if (fechaFin != null) {
            return fechaFin.plusDays(1).atStartOfDay();
        }
        return fechaInicio == null ? null : fechaInicio.plusDays(1).atStartOfDay();
    }

    /** Aplica alcance fino para doctores propios y secretarias asignadas. */
    private void verificarPermisoEdicionCita(Cita cita) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        if (autenticacion == null) {
            throw new AccesoDenegadoException("Debe autenticarse para editar citas");
        }
        Usuario usuario = obtenerUsuarioActual();
        boolean editaTodas = tieneAutoridad("ROLE_Administrador") || tieneAutoridad("citas.editar_todas");
        boolean editaPropias = tieneAutoridad("citas.editar_propias")
                && cita.getDoctor().getUsuario().getId().equals(usuario.getId());
        boolean editaAsignados = tieneAutoridad("citas.editar_asignados")
                && secretariaRepository.existsByUsuarioIdAndDoctoresId(usuario.getId(), cita.getDoctor().getId());
        if (!editaTodas && !editaPropias && !editaAsignados) {
            throw new AccesoDenegadoException("No tiene alcance para modificar esta cita");
        }
    }

    /** Aplica alcance fino para visualizacion de una cita puntual. */
    private void verificarPermisoVerCita(Cita cita) {
        Usuario usuario = obtenerUsuarioActual();
        boolean veTodas = tieneAutoridad("citas.ver_todas");
        boolean vePropia = tieneAutoridad("citas.ver_propias")
                && cita.getDoctor().getUsuario().getId().equals(usuario.getId());
        boolean veAsignada = tieneAutoridad("citas.ver_asignados")
                && secretariaRepository.existsByUsuarioIdAndDoctoresId(usuario.getId(), cita.getDoctor().getId());
        if (!veTodas && !vePropia && !veAsignada) {
            throw new AccesoDenegadoException("No tiene alcance para ver esta cita");
        }
    }

    /** Verifica que el doctor atienda en la sede indicada. */
    private void validarDoctorAtiendeEnSede(Doctor doctor, Sede sede) {
        boolean atiende = doctor.getSedes().stream().anyMatch(sedeDoctor -> sedeDoctor.getId().equals(sede.getId()));
        if (!atiende) {
            throw new BadRequestException("El doctor no atiende en la sede indicada");
        }
    }

    /** Obtiene usuario autenticado desde el contexto de seguridad. */
    private Usuario obtenerUsuarioActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        throw new AccesoDenegadoException("No se pudo resolver el usuario autenticado");
    }

    /** Comprueba si el usuario actual posee una autoridad. */
    private boolean tieneAutoridad(String permiso) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(permiso::equals);
    }

    /** Obtiene paciente o lanza 404. */
    private Paciente obtenerPaciente(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
    }

    /** Obtiene doctor o lanza 404. */
    private Doctor obtenerDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado"));
    }

    /** Obtiene sede o lanza 404. */
    private Sede obtenerSede(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
    }

    /** Obtiene cita vigente o lanza 404. */
    private Cita obtenerEntidad(Long id) {
        return citaRepository.findByIdAndEstadoNot(id, ESTADO_CANCELADA)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    /** Convierte cita a respuesta API. */
    private CitaResponse convertirRespuesta(Cita cita) {
        return CitaResponse.builder()
                .id(cita.getId())
                .pacienteNombre(cita.getPaciente().getNombres() + " " + cita.getPaciente().getApellidos())
                .doctorNombre(cita.getDoctor().getUsuario().getNombres() + " " + cita.getDoctor().getUsuario().getApellidos())
                .sedeNombre(cita.getSede().getNombre())
                .fechaHoraInicio(cita.getFechaHoraInicio())
                .fechaHoraFin(cita.getFechaHoraFin())
                .estado(cita.getEstado())
                .estadoPago(cita.getEstadoPago())
                .origen(cita.getOrigen())
                .build();
    }
}
