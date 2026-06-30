package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.DisponibilidadBaseCreateRequest;
import com.web.clinica.dto.request.ExcepcionDisponibilidadCreateRequest;
import com.web.clinica.dto.response.CitaConflictivaResponse;
import com.web.clinica.dto.response.DisponibilidadBaseResponse;
import com.web.clinica.dto.response.ExcepcionDisponibilidadResponse;
import com.web.clinica.exception.AccesoDenegadoException;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ConflictoDisponibilidadException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Cita;
import com.web.clinica.model.DisponibilidadBase;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.ExcepcionDisponibilidad;
import com.web.clinica.model.Sede;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.CitaRepository;
import com.web.clinica.repository.DisponibilidadBaseRepository;
import com.web.clinica.repository.DoctorRepository;
import com.web.clinica.repository.ExcepcionDisponibilidadRepository;
import com.web.clinica.repository.SedeRepository;
import com.web.clinica.service.abstractService.IDisponibilidadService;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DisponibilidadServiceImpl implements IDisponibilidadService {

    private final DisponibilidadBaseRepository disponibilidadBaseRepository;
    private final ExcepcionDisponibilidadRepository excepcionDisponibilidadRepository;
    private final DoctorRepository doctorRepository;
    private final SedeRepository sedeRepository;
    private final CitaRepository citaRepository;

    @Override
    @Transactional(readOnly = true)
    public List<DisponibilidadBaseResponse> listarBases(Long doctorId) {
        Doctor doctor = obtenerDoctorConAlcance(doctorId);
        return disponibilidadBaseRepository.findByDoctorIdOrderByDiaSemanaAscHoraInicioAsc(doctor.getId()).stream()
                .map(this::convertirBase)
                .toList();
    }

    @Override
    @Transactional
    public DisponibilidadBaseResponse guardarBase(Long doctorId, DisponibilidadBaseCreateRequest solicitud) {
        Doctor doctor = obtenerDoctorConAlcance(doctorId);
        Sede sede = obtenerSede(solicitud.getSedeId());
        validarRangoHorario(solicitud.getHoraInicio(), solicitud.getHoraFin());
        validarDoctorAtiendeEnSede(doctor, sede);

        DisponibilidadBase disponibilidad = disponibilidadBaseRepository
                .findByDoctorAndSedeAndDiaSemana(doctor, sede, solicitud.getDiaSemana())
                .stream()
                .findFirst()
                .orElseGet(DisponibilidadBase::new);

        // Validar solapamiento con cualquier disponibilidad base del mismo día para este doctor
        List<DisponibilidadBase> existentes = disponibilidadBaseRepository.findByDoctorIdOrderByDiaSemanaAscHoraInicioAsc(doctor.getId());
        for (DisponibilidadBase db : existentes) {
            if (db.getDiaSemana() == solicitud.getDiaSemana() && (disponibilidad.getId() == null || !db.getId().equals(disponibilidad.getId()))) {
                if (solicitud.getHoraInicio().isBefore(db.getHoraFin()) && solicitud.getHoraFin().isAfter(db.getHoraInicio())) {
                    throw new BadRequestException("El horario se solapa con un horario base existente (" 
                            + db.getHoraInicio() + " - " + db.getHoraFin() + " en " + db.getSede().getNombre() + ")");
                }
            }
        }

        disponibilidad.setDoctor(doctor);
        disponibilidad.setSede(sede);
        disponibilidad.setDiaSemana(solicitud.getDiaSemana());
        disponibilidad.setHoraInicio(solicitud.getHoraInicio());
        disponibilidad.setHoraFin(solicitud.getHoraFin());
        return convertirBase(disponibilidadBaseRepository.save(disponibilidad));
    }

    @Override
    @Transactional
    public void eliminarBase(Long doctorId, Long disponibilidadId) {
        Doctor doctor = obtenerDoctorConAlcance(doctorId);
        DisponibilidadBase disponibilidad = disponibilidadBaseRepository.findByIdAndDoctorId(disponibilidadId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Horario base no encontrado"));
        disponibilidadBaseRepository.delete(disponibilidad);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ExcepcionDisponibilidadResponse> listarExcepciones(Long doctorId, LocalDate fechaInicio, LocalDate fechaFin) {
        Doctor doctor = obtenerDoctorConAlcance(doctorId);
        LocalDate inicio = fechaInicio == null ? LocalDate.now() : fechaInicio;
        LocalDate fin = fechaFin == null ? inicio.plusDays(30) : fechaFin;
        if (fin.isBefore(inicio)) {
            throw new BadRequestException("La fecha final no puede ser anterior a la inicial");
        }
        return excepcionDisponibilidadRepository.findByDoctorIdAndFechaBetweenOrderByFechaAscHoraInicioAsc(doctor.getId(), inicio, fin)
                .stream()
                .map(this::convertirExcepcion)
                .toList();
    }

    @Override
    @Transactional
    public ExcepcionDisponibilidadResponse crearExcepcion(Long doctorId, ExcepcionDisponibilidadCreateRequest solicitud) {
        Doctor doctor = obtenerDoctorConAlcance(doctorId);
        validarRangoHorario(solicitud.getHoraInicio(), solicitud.getHoraFin());

        LocalDateTime excepcionInicio = solicitud.getFecha().atTime(solicitud.getHoraInicio());
        LocalDateTime excepcionFin = solicitud.getFecha().atTime(solicitud.getHoraFin());

        // Buscar citas que coincidan con el rango de la excepción
        List<Cita> citasConflicto = citaRepository.findByDoctorAndFechaHoraInicioBetween(
                doctor,
                solicitud.getFecha().atStartOfDay(),
                solicitud.getFecha().plusDays(1).atStartOfDay()
        ).stream()
                .filter(cita -> !"cancelada".equalsIgnoreCase(cita.getEstado()))
                .filter(cita -> {
                    return cita.getFechaHoraInicio().isBefore(excepcionFin) && cita.getFechaHoraFin().isAfter(excepcionInicio);
                })
                .toList();

        if (!citasConflicto.isEmpty()) {
            List<CitaConflictivaResponse> conflictoResponses = citasConflicto.stream()
                    .map(cita -> CitaConflictivaResponse.builder()
                            .id(cita.getId())
                            .pacienteDni(cita.getPaciente().getDni())
                            .pacienteNombre(cita.getPaciente().getNombres() + " " + cita.getPaciente().getApellidos())
                            .fechaHoraInicio(cita.getFechaHoraInicio())
                            .fechaHoraFin(cita.getFechaHoraFin())
                            .build())
                    .toList();
            throw new ConflictoDisponibilidadException(
                    "Existen citas programadas en el rango de la excepción. Debe reprogramarlas o reasignarlas.",
                    conflictoResponses
            );
        }

        ExcepcionDisponibilidad excepcion = new ExcepcionDisponibilidad();
        excepcion.setDoctor(doctor);
        excepcion.setFecha(solicitud.getFecha());
        excepcion.setHoraInicio(solicitud.getHoraInicio());
        excepcion.setHoraFin(solicitud.getHoraFin());
        excepcion.setMotivo(solicitud.getMotivo());
        return convertirExcepcion(excepcionDisponibilidadRepository.save(excepcion));
    }

    @Override
    @Transactional
    public void eliminarExcepcion(Long doctorId, Long excepcionId) {
        Doctor doctor = obtenerDoctorConAlcance(doctorId);
        ExcepcionDisponibilidad excepcion = excepcionDisponibilidadRepository.findByIdAndDoctorId(excepcionId, doctor.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Excepcion de disponibilidad no encontrada"));
        excepcionDisponibilidadRepository.delete(excepcion);
    }

    private Doctor obtenerDoctorConAlcance(Long doctorId) {
        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado"));
        if (tieneAutoridad("ROLE_Administrador") || tieneAutoridad("disponibilidad.ver_todas")) {
            return doctor;
        }
        Usuario usuario = obtenerUsuarioActual();
        if (tieneAutoridad("disponibilidad.ver_propia") && doctor.getUsuario().getId().equals(usuario.getId())) {
            return doctor;
        }
        throw new AccesoDenegadoException("No tiene alcance para gestionar esta disponibilidad");
    }

    private void validarRangoHorario(java.time.LocalTime inicio, java.time.LocalTime fin) {
        if (!inicio.isBefore(fin)) {
            throw new BadRequestException("La hora de inicio debe ser anterior a la hora de fin");
        }
    }

    private void validarDoctorAtiendeEnSede(Doctor doctor, Sede sede) {
        boolean atiende = doctor.getSedes().stream().anyMatch(sedeDoctor -> sedeDoctor.getId().equals(sede.getId()));
        if (!atiende) {
            throw new BadRequestException("El doctor no atiende en la sede indicada");
        }
    }

    private Sede obtenerSede(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
    }

    private Usuario obtenerUsuarioActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        throw new AccesoDenegadoException("No se pudo resolver el usuario autenticado");
    }

    private boolean tieneAutoridad(String permiso) {
        Authentication autenticacion = SecurityContextHolder.getContext().getAuthentication();
        return autenticacion != null && autenticacion.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(permiso::equals);
    }

    private DisponibilidadBaseResponse convertirBase(DisponibilidadBase disponibilidad) {
        return DisponibilidadBaseResponse.builder()
                .id(disponibilidad.getId())
                .doctorId(disponibilidad.getDoctor().getId())
                .sedeId(disponibilidad.getSede().getId())
                .sedeNombre(disponibilidad.getSede().getNombre())
                .diaSemana(disponibilidad.getDiaSemana())
                .horaInicio(disponibilidad.getHoraInicio())
                .horaFin(disponibilidad.getHoraFin())
                .build();
    }

    private ExcepcionDisponibilidadResponse convertirExcepcion(ExcepcionDisponibilidad excepcion) {
        return ExcepcionDisponibilidadResponse.builder()
                .id(excepcion.getId())
                .doctorId(excepcion.getDoctor().getId())
                .fecha(excepcion.getFecha())
                .horaInicio(excepcion.getHoraInicio())
                .horaFin(excepcion.getHoraFin())
                .motivo(excepcion.getMotivo())
                .build();
    }
}
