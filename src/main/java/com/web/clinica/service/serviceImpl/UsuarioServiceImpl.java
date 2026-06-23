package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.UsuarioCreateRequest;
import com.web.clinica.dto.request.UsuarioUpdateRequest;
import com.web.clinica.dto.response.RolResponse;
import com.web.clinica.dto.response.UsuarioResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Rol;
import com.web.clinica.model.Secretaria;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.DoctorRepository;
import com.web.clinica.repository.RolRepository;
import com.web.clinica.repository.SecretariaRepository;
import com.web.clinica.repository.UsuarioRepository;
import com.web.clinica.service.abstractService.IUsuarioService;
import com.web.clinica.util.EmailService;
import java.security.SecureRandom;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

@Service
@RequiredArgsConstructor
public class UsuarioServiceImpl implements IUsuarioService {

    private static final String CARACTERES_PASSWORD = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnopqrstuvwxyz23456789";

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final SecretariaRepository secretariaRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final SecureRandom generadorSeguro;

    @Override
    @Transactional(readOnly = true)
    public Page<UsuarioResponse> listar(Pageable pageable) {
        return usuarioRepository.findAll(pageable).map(this::convertirRespuesta);
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPorId(Long id) {
        return convertirRespuesta(obtenerEntidad(id));
    }

    @Override
    @Transactional
    public UsuarioResponse crear(UsuarioCreateRequest solicitud) {
        usuarioRepository.findByDni(solicitud.getDni()).ifPresent(u -> {
            throw new BadRequestException("Ya existe un usuario con ese DNI");
        });
        usuarioRepository.findByEmail(solicitud.getEmail()).ifPresent(u -> {
            throw new BadRequestException("Ya existe un usuario con ese email");
        });

        List<Rol> roles = rolRepository.findAllById(solicitud.getRolesIds());
        if (roles.size() != Set.copyOf(solicitud.getRolesIds()).size()) {
            throw new BadRequestException("Uno o más roles no existen");
        }

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
        usuario.setRoles(new HashSet<>(roles));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        boolean esSecretaria = roles.stream().anyMatch(r -> r.getNombre().equalsIgnoreCase("Secretaria"));
        if (esSecretaria) {
            Secretaria secretaria = new Secretaria();
            secretaria.setUsuario(usuarioGuardado);
            if (!CollectionUtils.isEmpty(solicitud.getDoctorIds())) {
                List<Doctor> doctores = doctorRepository.findAllById(solicitud.getDoctorIds());
                if (doctores.size() != Set.copyOf(solicitud.getDoctorIds()).size()) {
                    throw new BadRequestException("Uno o más médicos asignados no existen");
                }
                secretaria.setDoctores(new HashSet<>(doctores));
            }
            secretariaRepository.save(secretaria);
        }

        emailService.enviarCorreo(
                usuarioGuardado.getEmail(),
                "Credenciales temporales - Clinica",
                "Su usuario es su DNI y su password temporal es: " + passwordTemporal
        );

        return convertirRespuesta(usuarioGuardado);
    }

    @Override
    @Transactional
    public UsuarioResponse actualizar(Long id, UsuarioUpdateRequest solicitud) {
        Usuario usuario = obtenerEntidad(id);

        usuarioRepository.findByEmail(solicitud.getEmail())
                .filter(u -> !u.getId().equals(id))
                .ifPresent(u -> {
                    throw new BadRequestException("Ya existe otro usuario con ese email");
                });

        List<Rol> roles = rolRepository.findAllById(solicitud.getRolesIds());
        if (roles.size() != Set.copyOf(solicitud.getRolesIds()).size()) {
            throw new BadRequestException("Uno o más roles no existen");
        }

        usuario.setNombres(solicitud.getNombres());
        usuario.setApellidos(solicitud.getApellidos());
        usuario.setEmail(solicitud.getEmail());
        usuario.setTelefono(solicitud.getTelefono());
        usuario.setFechaNacimiento(solicitud.getFechaNacimiento());
        usuario.setRoles(new HashSet<>(roles));

        Usuario usuarioGuardado = usuarioRepository.save(usuario);

        boolean esSecretaria = roles.stream().anyMatch(r -> r.getNombre().equalsIgnoreCase("Secretaria"));
        if (esSecretaria) {
            Secretaria secretaria = secretariaRepository.findByUsuarioId(id)
                    .orElseGet(() -> {
                        Secretaria s = new Secretaria();
                        s.setUsuario(usuarioGuardado);
                        return s;
                    });

            if (!CollectionUtils.isEmpty(solicitud.getDoctorIds())) {
                List<Doctor> doctores = doctorRepository.findAllById(solicitud.getDoctorIds());
                if (doctores.size() != Set.copyOf(solicitud.getDoctorIds()).size()) {
                    throw new BadRequestException("Uno o más médicos asignados no existen");
                }
                secretaria.setDoctores(new HashSet<>(doctores));
            } else {
                secretaria.setDoctores(new HashSet<>());
            }
            secretariaRepository.save(secretaria);
        } else {
            secretariaRepository.findByUsuarioId(id).ifPresent(secretariaRepository::delete);
        }

        return convertirRespuesta(usuarioGuardado);
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Usuario usuario = obtenerEntidad(id);
        usuario.setActivo(!Boolean.TRUE.equals(usuario.getActivo())); // toggle or set to false?
        // Wait, "desactivar" permission and rule "usuarios.desactivar" -> "Activar/desactivar empleado"
        // and Falta_Implementar.md says: "usuarios.desactivar: Activar/desactivar empleado"
        // So toggle is perfect! Let's make it toggle.
        usuarioRepository.save(usuario);
    }

    private Usuario obtenerEntidad(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
    }

    private String generarPasswordTemporal() {
        StringBuilder password = new StringBuilder();
        for (int indice = 0; indice < 8; indice++) {
            int posicion = generadorSeguro.nextInt(CARACTERES_PASSWORD.length());
            password.append(CARACTERES_PASSWORD.charAt(posicion));
        }
        return password.toString();
    }

    private UsuarioResponse convertirRespuesta(Usuario usuario) {
        List<Long> doctorIds = List.of();
        boolean esSecretaria = usuario.getRoles().stream().anyMatch(r -> r.getNombre().equalsIgnoreCase("Secretaria"));
        if (esSecretaria) {
            doctorIds = secretariaRepository.findByUsuarioId(usuario.getId())
                    .map(s -> s.getDoctores().stream().map(Doctor::getId).sorted().toList())
                    .orElse(List.of());
        }

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .dni(usuario.getDni())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .fechaNacimiento(usuario.getFechaNacimiento())
                .activo(usuario.getActivo())
                .roles(usuario.getRoles().stream().map(this::convertirRol).toList())
                .doctorIds(doctorIds)
                .build();
    }

    private RolResponse convertirRol(Rol rol) {
        return RolResponse.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .build();
    }
}
