package com.web.clinica.service.serviceImpl;

import com.web.clinica.config.JwtProvider;
import com.web.clinica.dto.request.CambioPasswordRequest;
import com.web.clinica.dto.request.LoginRequest;
import com.web.clinica.dto.request.MiPerfilRequest;
import com.web.clinica.dto.request.RecuperarPasswordRequest;
import com.web.clinica.dto.request.ValidarCodigoRequest;
import com.web.clinica.dto.request.VerificarCodigoRecuperacionRequest;
import com.web.clinica.dto.response.JwtResponse;
import com.web.clinica.dto.response.RolResponse;
import com.web.clinica.dto.response.UsuarioResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.exception.UnauthorizedException;
import com.web.clinica.model.CodigoVerificacion;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Permiso;
import com.web.clinica.model.Rol;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.CodigoVerificacionRepository;
import com.web.clinica.repository.SecretariaRepository;
import com.web.clinica.repository.UsuarioRepository;
import com.web.clinica.service.abstractService.IAuthService;
import com.web.clinica.util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements IAuthService, UserDetailsService {

    private static final String TIPO_RECUPERACION = "recuperacion";
    private static final int MINIMO_PASSWORD = 8;

    private final UsuarioRepository usuarioRepository;
    private final CodigoVerificacionRepository codigoVerificacionRepository;
    private final SecretariaRepository secretariaRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final EmailService emailService;
    private final SecureRandom generadorSeguro;

    @Autowired
    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           CodigoVerificacionRepository codigoVerificacionRepository,
                           SecretariaRepository secretariaRepository,
                           PasswordEncoder passwordEncoder,
                           JwtProvider jwtProvider,
                           EmailService emailService,
                           SecureRandom generadorSeguro) {
        this.usuarioRepository = usuarioRepository;
        this.codigoVerificacionRepository = codigoVerificacionRepository;
        this.secretariaRepository = secretariaRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.emailService = emailService;
        this.generadorSeguro = generadorSeguro;
    }

    // Constructor para retrocompatibilidad con tests (6 argumentos)
    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           CodigoVerificacionRepository codigoVerificacionRepository,
                           PasswordEncoder passwordEncoder,
                           JwtProvider jwtProvider,
                           EmailService emailService,
                           SecureRandom generadorSeguro) {
        this.usuarioRepository = usuarioRepository;
        this.codigoVerificacionRepository = codigoVerificacionRepository;
        this.secretariaRepository = null;
        this.passwordEncoder = passwordEncoder;
        this.jwtProvider = jwtProvider;
        this.emailService = emailService;
        this.generadorSeguro = generadorSeguro;
    }

    /** Autentica con DNI y password, luego construye la respuesta JWT. */
    @Override
    @Transactional(readOnly = true)
    public JwtResponse iniciarSesion(LoginRequest solicitud) {
        Usuario usuario = usuarioRepository.findByDni(solicitud.getDni())
                .orElseThrow(() -> new UnauthorizedException("DNI o password incorrectos"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("El usuario esta desactivado");
        }

        if (!passwordEncoder.matches(solicitud.getPassword(), usuario.getPasswordHash())) {
            throw new UnauthorizedException("DNI o password incorrectos");
        }

        return construirJwtResponse(usuario);
    }


    /** Reconstruye JWT y permisos vigentes desde base de datos. */
    @Override
    @Transactional(readOnly = true)
    public JwtResponse obtenerSesionActual(Long usuarioId) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));
        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("El usuario esta desactivado");
        }
        return construirJwtResponse(usuario);
    }

    /** Cambia el password y desactiva la obligacion del primer inicio. */
    @Override
    @Transactional
    public void cambiarPassword(Long usuarioId, CambioPasswordRequest solicitud) {
        validarPassword(solicitud.getNuevaPassword(), solicitud.getRepetirPassword());
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setPasswordHash(passwordEncoder.encode(solicitud.getNuevaPassword()));
        usuario.setCambioPasswordObligatorio(false);
        usuarioRepository.save(usuario);
    }

    /** Genera y guarda un codigo de recuperacion asociado al correo. */
    @Override
    @Transactional
    public void generarCodigoRecuperacion(RecuperarPasswordRequest solicitud) {
        Usuario usuario = usuarioRepository.findByDniAndEmail(solicitud.getDni(), solicitud.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("No existe un usuario con ese DNI y correo"));

        CodigoVerificacion codigoVerificacion = new CodigoVerificacion();
        codigoVerificacion.setEmail(usuario.getEmail());
        codigoVerificacion.setCodigo(generarCodigoNumerico());
        codigoVerificacion.setTipo(TIPO_RECUPERACION);
        codigoVerificacion.setUsado(false);
        codigoVerificacion.setFechaExpiracion(LocalDateTime.now().plusMinutes(15));
        codigoVerificacionRepository.save(codigoVerificacion);

        emailService.enviarCorreo(
                usuario.getEmail(),
                "Codigo de recuperacion",
                "Su codigo de recuperacion es: " + codigoVerificacion.getCodigo()
        );
    }

    /** Valida que un codigo de recuperacion sea vigente y no usado. */
    @Override
    @Transactional(readOnly = true)
    public void validarCodigoRecuperacion(ValidarCodigoRequest solicitud) {
        codigoVerificacionRepository
                .findByEmailAndCodigoAndUsadoFalseAndFechaExpiracionAfter(
                        solicitud.getEmail(),
                        solicitud.getCodigo(),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new BadRequestException("Codigo invalido o expirado"));
    }

    /** Valida un codigo de recuperacion y reemplaza el password. */
    @Override
    @Transactional
    public void restablecerPasswordConCodigo(VerificarCodigoRecuperacionRequest solicitud) {
        validarPassword(solicitud.getNuevaPassword(), solicitud.getRepetirPassword());
        CodigoVerificacion codigoVerificacion = codigoVerificacionRepository
                .findByEmailAndCodigoAndUsadoFalseAndFechaExpiracionAfter(
                        solicitud.getEmail(),
                        solicitud.getCodigo(),
                        LocalDateTime.now()
                )
                .orElseThrow(() -> new BadRequestException("Codigo invalido o expirado"));

        Usuario usuario = usuarioRepository.findByEmail(solicitud.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setPasswordHash(passwordEncoder.encode(solicitud.getNuevaPassword()));
        usuario.setCambioPasswordObligatorio(false);
        codigoVerificacion.setUsado(true);

        usuarioRepository.save(usuario);
        codigoVerificacionRepository.save(codigoVerificacion);
    }

    /** Carga usuarios por DNI para el filtro JWT de Spring Security. */
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String dni) throws UsernameNotFoundException {
        return usuarioRepository.findByDni(dni)
                .orElseThrow(() -> new UsernameNotFoundException("Usuario no encontrado"));
    }

    /** Valida coincidencia y longitud minima de password. */
    private void validarPassword(String nuevaPassword, String repetirPassword) {
        if (!nuevaPassword.equals(repetirPassword)) {
            throw new BadRequestException("Las passwords no coinciden");
        }
        if (nuevaPassword.length() < MINIMO_PASSWORD) {
            throw new BadRequestException("La password debe tener al menos 8 caracteres");
        }
    }

    /** Construye la respuesta de login con roles y permisos ordenados. */
    private JwtResponse construirJwtResponse(Usuario usuario) {
        List<String> roles = usuario.getRoles().stream()
                .map(Rol::getNombre)
                .sorted()
                .toList();
        List<String> permisos = usuario.getRoles().stream()
                .flatMap(rol -> rol.getPermisos().stream())
                .map(Permiso::getCodigo)
                .distinct()
                .sorted(Comparator.naturalOrder())
                .toList();

        return JwtResponse.builder()
                .token(jwtProvider.generarToken(usuario))
                .dni(usuario.getDni())
                .nombres(usuario.getNombres())
                .apellidos(usuario.getApellidos())
                .email(usuario.getEmail())
                .telefono(usuario.getTelefono())
                .cambioPasswordObligatorio(Boolean.TRUE.equals(usuario.getCambioPasswordObligatorio()))
                .roles(roles)
                .permisos(permisos)
                .build();
    }

    /** Genera un codigo numerico de seis digitos. */
    private String generarCodigoNumerico() {
        return String.format("%06d", generadorSeguro.nextInt(1_000_000));
    }

    @Override
    @Transactional
    public UsuarioResponse actualizarMiPerfil(Long usuarioId, MiPerfilRequest solicitud) {
        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        usuario.setEmail(solicitud.getEmail());
        usuario.setTelefono(solicitud.getTelefono());

        if (solicitud.getNuevaPassword() != null && !solicitud.getNuevaPassword().isBlank()) {
            if (solicitud.getPasswordAnterior() == null || solicitud.getPasswordAnterior().isBlank()) {
                throw new BadRequestException("Debe ingresar la contraseña anterior para poder cambiarla");
            }
            if (!passwordEncoder.matches(solicitud.getPasswordAnterior(), usuario.getPasswordHash())) {
                throw new BadRequestException("La contraseña anterior es incorrecta");
            }
            validarPassword(solicitud.getNuevaPassword(), solicitud.getRepetirPassword());
            usuario.setPasswordHash(passwordEncoder.encode(solicitud.getNuevaPassword()));
            usuario.setCambioPasswordObligatorio(false);
        }

        Usuario usuarioGuardado = usuarioRepository.save(usuario);
        return convertirUsuarioRespuesta(usuarioGuardado);
    }

    private UsuarioResponse convertirUsuarioRespuesta(Usuario usuario) {
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
