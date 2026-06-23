package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.PacienteCreateRequest;
import com.web.clinica.dto.request.PacienteUpdateRequest;
import com.web.clinica.dto.response.PacienteResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Paciente;
import com.web.clinica.repository.PacienteRepository;
import com.web.clinica.service.abstractService.IPacienteService;
import com.web.clinica.util.DniApiClient;
import com.web.clinica.util.DniInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class PacienteServiceImpl implements IPacienteService {

    private final PacienteRepository pacienteRepository;
    private final PasswordEncoder passwordEncoder;
    private final DniApiClient dniApiClient;

    /** Crea un paciente y encripta password web cuando se proporciona. */
    @Override
    @Transactional
    public PacienteResponse crear(PacienteCreateRequest solicitud) {
        pacienteRepository.findByDni(solicitud.getDni()).ifPresent(paciente -> {
            throw new BadRequestException("Ya existe un paciente con ese DNI");
        });

        Paciente paciente = Paciente.builder()
                .dni(solicitud.getDni())
                .nombres(solicitud.getNombres())
                .apellidos(solicitud.getApellidos())
                .sexo(solicitud.getSexo())
                .fechaNacimiento(solicitud.getFechaNacimiento())
                .telefono(solicitud.getTelefono())
                .email(solicitud.getEmail())
                .passwordHash(generarHashOpcional(solicitud.getPassword()))
                .activo(true)
                .build();
        return convertirRespuesta(pacienteRepository.save(paciente));
    }

    /** Actualiza datos editables y reemplaza password web si llega uno nuevo. */
    @Override
    @Transactional
    public PacienteResponse actualizar(Long id, PacienteUpdateRequest solicitud) {
        Paciente paciente = obtenerEntidad(id);
        paciente.setNombres(solicitud.getNombres());
        paciente.setApellidos(solicitud.getApellidos());
        paciente.setSexo(solicitud.getSexo());
        paciente.setFechaNacimiento(solicitud.getFechaNacimiento());
        paciente.setTelefono(solicitud.getTelefono());
        paciente.setEmail(solicitud.getEmail());
        if (StringUtils.hasText(solicitud.getPassword())) {
            paciente.setPasswordHash(passwordEncoder.encode(solicitud.getPassword()));
        }
        return convertirRespuesta(pacienteRepository.save(paciente));
    }

    /** Obtiene un paciente existente. */
    @Override
    @Transactional(readOnly = true)
    public PacienteResponse obtenerPorId(Long id) {
        return convertirRespuesta(obtenerEntidad(id));
    }

    /** Busca un paciente existente por DNI. */
    @Override
    @Transactional(readOnly = true)
    public PacienteResponse buscarPorDni(String dni) {
        return pacienteRepository.findByDni(dni)
                .map(this::convertirRespuesta)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
    }

    /** Consulta el servicio externo de DNI y devuelve un DTO sin persistir. */
    @Override
    public PacienteResponse consultarDni(String dni) {
        DniInfo info = dniApiClient.consultarDni(dni);
        return PacienteResponse.builder()
                .dni(info.dni())
                .nombres(info.nombres())
                .apellidos(info.apellidos())
                .build();
    }

    /** Lista solo pacientes activos. */
    @Override
    @Transactional(readOnly = true)
    public Page<PacienteResponse> listarActivos(Pageable pageable) {
        return pacienteRepository.findByActivoTrue(pageable).map(this::convertirRespuesta);
    }

    /** Marca al paciente como inactivo. */
    @Override
    @Transactional
    public void desactivar(Long id) {
        Paciente paciente = obtenerEntidad(id);
        paciente.setActivo(false);
        pacienteRepository.save(paciente);
    }

    /** Recupera entidad o lanza 404. */
    private Paciente obtenerEntidad(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
    }

    /** Encripta un password solo si tiene texto. */
    private String generarHashOpcional(String password) {
        return StringUtils.hasText(password) ? passwordEncoder.encode(password) : null;
    }

    /** Convierte la entidad paciente al DTO de respuesta. */
    private PacienteResponse convertirRespuesta(Paciente paciente) {
        return PacienteResponse.builder()
                .id(paciente.getId())
                .dni(paciente.getDni())
                .nombres(paciente.getNombres())
                .apellidos(paciente.getApellidos())
                .sexo(paciente.getSexo())
                .fechaNacimiento(paciente.getFechaNacimiento())
                .telefono(paciente.getTelefono())
                .email(paciente.getEmail())
                .activo(paciente.getActivo())
                .build();
    }
}
