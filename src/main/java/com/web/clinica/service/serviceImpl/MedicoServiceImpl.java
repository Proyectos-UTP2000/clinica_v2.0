package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.MedicoCreateRequest;
import com.web.clinica.dto.request.MedicoUpdateRequest;
import com.web.clinica.dto.response.MedicoResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Especialidad;
import com.web.clinica.model.Rol;
import com.web.clinica.model.Sede;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.DoctorRepository;
import com.web.clinica.repository.EspecialidadRepository;
import com.web.clinica.repository.RolRepository;
import com.web.clinica.repository.SedeRepository;
import com.web.clinica.repository.UsuarioRepository;
import com.web.clinica.service.abstractService.IMedicoService;
import com.web.clinica.util.EmailService;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class MedicoServiceImpl implements IMedicoService {

    private static final String CARACTERES_PASSWORD = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final DoctorRepository doctorRepository;
    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final SedeRepository sedeRepository;
    private final EmailService emailService;
    private final PasswordEncoder passwordEncoder;
    private final SecureRandom generadorSeguro;

    /** Crea usuario interno, asigna rol medico y crea perfil doctor. */
    @Override
    @Transactional
    public MedicoResponse crear(MedicoCreateRequest solicitud) {
        usuarioRepository.findByDni(solicitud.getDni()).ifPresent(usuario -> {
            throw new BadRequestException("Ya existe un usuario con ese DNI");
        });
        usuarioRepository.findByEmail(solicitud.getEmail()).ifPresent(usuario -> {
            throw new BadRequestException("Ya existe un usuario con ese email");
        });

        String passwordTemporal = generarPasswordTemporal();
        Usuario usuario = new Usuario();
        usuario.setDni(solicitud.getDni());
        usuario.setNombres(solicitud.getNombres());
        usuario.setApellidos(solicitud.getApellidos());
        usuario.setEmail(solicitud.getEmail());
        usuario.setTelefono(solicitud.getTelefono());
        usuario.setFechaNacimiento(solicitud.getFechaNacimiento());
        usuario.setPasswordHash(passwordEncoder.encode(passwordTemporal));
        usuario.setCambioPasswordObligatorio(true);
        usuario.setActivo(true);
        usuario.getRoles().add(obtenerRolMedico());

        Doctor doctor = new Doctor();
        doctor.setUsuario(usuarioRepository.save(usuario));
        aplicarDatosMedicos(doctor, solicitud.getEspecialidadId(), solicitud.getSubespecialidadId(), solicitud.getSedesIds());
        Doctor doctorGuardado = doctorRepository.save(doctor);

        emailService.enviarCorreo(
                usuario.getEmail(),
                "Credenciales temporales - Clinica",
                "Su usuario es su DNI y su password temporal es: " + passwordTemporal
        );
        return convertirRespuesta(doctorGuardado);
    }

    /** Actualiza datos del usuario y datos medicos asociados. */
    @Override
    @Transactional
    public MedicoResponse actualizar(Long id, MedicoUpdateRequest solicitud) {
        Doctor doctor = obtenerEntidad(id);
        Usuario usuario = doctor.getUsuario();
        usuario.setNombres(solicitud.getNombres());
        usuario.setApellidos(solicitud.getApellidos());
        usuario.setEmail(solicitud.getEmail());
        usuario.setTelefono(solicitud.getTelefono());
        usuario.setFechaNacimiento(solicitud.getFechaNacimiento());
        aplicarDatosMedicos(doctor, solicitud.getEspecialidadId(), solicitud.getSubespecialidadId(), solicitud.getSedesIds());
        usuarioRepository.save(usuario);
        return convertirRespuesta(doctorRepository.save(doctor));
    }

    /** Obtiene un medico por id. */
    @Override
    @Transactional(readOnly = true)
    public MedicoResponse obtenerPorId(Long id) {
        return convertirRespuesta(obtenerEntidad(id));
    }

    /** Lista doctores activos con filtros opcionales. */
    @Override
    @Transactional(readOnly = true)
    public Page<MedicoResponse> listarActivos(String texto, Long especialidadId, Long sedeId, Pageable pageable) {
        return doctorRepository.listarConFiltros(texto, especialidadId, sedeId, pageable).map(this::convertirRespuesta);
    }

    /** Desactiva el usuario asociado al doctor. */
    @Override
    @Transactional
    public void desactivar(Long id) {
        Doctor doctor = obtenerEntidad(id);
        doctor.getUsuario().setActivo(false);
        usuarioRepository.save(doctor.getUsuario());
    }

    /** Obtiene la entidad doctor para otros servicios. */
    public Doctor obtenerEntidad(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Medico no encontrado"));
    }

    /** Aplica especialidad, subespecialidad y sedes al doctor. */
    private void aplicarDatosMedicos(Doctor doctor, Long especialidadId, Long subespecialidadId, List<Long> sedesIds) {
        Especialidad especialidad = especialidadRepository.findById(especialidadId)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada"));
        doctor.setEspecialidad(especialidad);
        doctor.setSubespecialidad(subespecialidadId == null ? null : especialidadRepository.findById(subespecialidadId)
                .orElseThrow(() -> new ResourceNotFoundException("Subespecialidad no encontrada")));
        doctor.setSedes(obtenerSedes(sedesIds));
    }

    /** Obtiene las sedes solicitadas y valida que existan todas. */
    private Set<Sede> obtenerSedes(List<Long> sedesIds) {
        if (CollectionUtils.isEmpty(sedesIds)) {
            return new HashSet<>();
        }
        List<Sede> sedes = sedeRepository.findAllById(sedesIds);
        if (sedes.size() != new HashSet<>(sedesIds).size()) {
            throw new ResourceNotFoundException("Una o mas sedes no existen");
        }
        return new HashSet<>(sedes);
    }

    /** Busca el rol medico aceptando nombres habituales de semilla. */
    private Rol obtenerRolMedico() {
        return rolRepository.findByNombre("Doctor")
                .or(() -> rolRepository.findByNombre("DOCTOR"))
                .or(() -> rolRepository.findByNombre("Medico"))
                .orElseThrow(() -> new ResourceNotFoundException("Rol Doctor no encontrado"));
    }

    /** Genera password temporal legible de ocho caracteres. */
    private String generarPasswordTemporal() {
        StringBuilder password = new StringBuilder();
        for (int indice = 0; indice < 8; indice++) {
            int posicion = generadorSeguro.nextInt(CARACTERES_PASSWORD.length());
            password.append(CARACTERES_PASSWORD.charAt(posicion));
        }
        return password.toString();
    }

    /** Convierte doctor a DTO de salida. */
    private MedicoResponse convertirRespuesta(Doctor doctor) {
        Usuario usuario = doctor.getUsuario();
        return MedicoResponse.builder()
                .id(doctor.getId())
                .usuarioId(usuario.getId())
                .dni(usuario.getDni())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .especialidadNombre(doctor.getEspecialidad().getNombre())
                .subespecialidadNombre(doctor.getSubespecialidad() == null ? null : doctor.getSubespecialidad().getNombre())
                .sedes(doctor.getSedes().stream().map(Sede::getNombre).sorted().toList())
                .activo(usuario.getActivo())
                .build();
    }
}
