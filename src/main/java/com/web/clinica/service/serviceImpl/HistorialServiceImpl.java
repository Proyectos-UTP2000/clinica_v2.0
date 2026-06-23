package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.AdjuntoRequest;
import com.web.clinica.dto.request.ConsultaCreateRequest;
import com.web.clinica.dto.request.EstudioRequest;
import com.web.clinica.dto.request.IndicacionRequest;
import com.web.clinica.dto.request.NotaEvolucionRequest;
import com.web.clinica.dto.request.RecetaRequest;
import com.web.clinica.dto.response.AdjuntoDownloadResponse;
import com.web.clinica.dto.response.AdjuntoResponse;
import com.web.clinica.dto.response.ConsultaResponse;
import com.web.clinica.dto.response.EstudioResponse;
import com.web.clinica.dto.response.IndicacionResponse;
import com.web.clinica.dto.response.NotaEvolucionResponse;
import com.web.clinica.dto.response.RecetaResponse;
import com.web.clinica.exception.AccesoDenegadoException;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Adjunto;
import com.web.clinica.model.Cita;
import com.web.clinica.model.Consulta;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.EstudioComplementario;
import com.web.clinica.model.IndicacionMedica;
import com.web.clinica.model.NotaEvolucion;
import com.web.clinica.model.Paciente;
import com.web.clinica.model.Receta;
import com.web.clinica.model.Sede;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.AdjuntoRepository;
import com.web.clinica.repository.CitaRepository;
import com.web.clinica.repository.ConsultaRepository;
import com.web.clinica.repository.DoctorRepository;
import com.web.clinica.repository.EstudioComplementarioRepository;
import com.web.clinica.repository.IndicacionMedicaRepository;
import com.web.clinica.repository.NotaEvolucionRepository;
import com.web.clinica.repository.PacienteRepository;
import com.web.clinica.repository.RecetaRepository;
import com.web.clinica.repository.SedeRepository;
import com.web.clinica.service.abstractService.IHistorialService;
import java.time.LocalDateTime;
import java.util.List;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class HistorialServiceImpl implements IHistorialService {

    private static final List<String> TIPOS_CONSULTA = List.of("consulta", "control", "urgencia", "procedimiento");
    private static final List<String> TIPOS_INDICACION = List.of("reposo", "derivacion", "estudios");

    private final ConsultaRepository consultaRepository;
    private final RecetaRepository recetaRepository;
    private final IndicacionMedicaRepository indicacionMedicaRepository;
    private final EstudioComplementarioRepository estudioComplementarioRepository;
    private final AdjuntoRepository adjuntoRepository;
    private final NotaEvolucionRepository notaEvolucionRepository;
    private final PacienteRepository pacienteRepository;
    private final DoctorRepository doctorRepository;
    private final SedeRepository sedeRepository;
    private final CitaRepository citaRepository;
    private final Path adjuntosStorageDir;

    @Autowired
    public HistorialServiceImpl(ConsultaRepository consultaRepository,
                                RecetaRepository recetaRepository,
                                IndicacionMedicaRepository indicacionMedicaRepository,
                                EstudioComplementarioRepository estudioComplementarioRepository,
                                AdjuntoRepository adjuntoRepository,
                                NotaEvolucionRepository notaEvolucionRepository,
                                PacienteRepository pacienteRepository,
                                DoctorRepository doctorRepository,
                                SedeRepository sedeRepository,
                                CitaRepository citaRepository,
                                @Value("${app.storage.adjuntos-dir:uploads/adjuntos}") String adjuntosStorageDir) {
        this(consultaRepository, recetaRepository, indicacionMedicaRepository, estudioComplementarioRepository,
                adjuntoRepository, notaEvolucionRepository, pacienteRepository, doctorRepository, sedeRepository,
                citaRepository, Path.of(adjuntosStorageDir));
    }

    public HistorialServiceImpl(ConsultaRepository consultaRepository,
                                RecetaRepository recetaRepository,
                                IndicacionMedicaRepository indicacionMedicaRepository,
                                EstudioComplementarioRepository estudioComplementarioRepository,
                                AdjuntoRepository adjuntoRepository,
                                NotaEvolucionRepository notaEvolucionRepository,
                                PacienteRepository pacienteRepository,
                                DoctorRepository doctorRepository,
                                SedeRepository sedeRepository,
                                CitaRepository citaRepository,
                                Path adjuntosStorageDir) {
        this.consultaRepository = consultaRepository;
        this.recetaRepository = recetaRepository;
        this.indicacionMedicaRepository = indicacionMedicaRepository;
        this.estudioComplementarioRepository = estudioComplementarioRepository;
        this.adjuntoRepository = adjuntoRepository;
        this.notaEvolucionRepository = notaEvolucionRepository;
        this.pacienteRepository = pacienteRepository;
        this.doctorRepository = doctorRepository;
        this.sedeRepository = sedeRepository;
        this.citaRepository = citaRepository;
        this.adjuntosStorageDir = adjuntosStorageDir;
    }

    /** Crea una consulta clinica y sus detalles en una sola transaccion. */
    @Override
    @Transactional
    public ConsultaResponse crearConsulta(ConsultaCreateRequest solicitud) {
        validarTipoConsulta(solicitud.getTipo());
        Paciente paciente = obtenerPaciente(solicitud.getPacienteId());
        Doctor doctor = obtenerDoctor(solicitud.getDoctorId());
        Sede sede = obtenerSede(solicitud.getSedeId());
        Cita cita = obtenerCitaOpcional(solicitud.getCitaId());

        validarAlcanceCreacion(doctor);
        validarCitaCompatible(cita, paciente, doctor, sede);

        if (cita != null) {
            cita.setEstado("atendida");
            citaRepository.save(cita);
        }

        Consulta consulta = Consulta.builder()
                .paciente(paciente)
                .doctor(doctor)
                .sede(sede)
                .cita(cita)
                .fechaHora(LocalDateTime.now())
                .tipo(solicitud.getTipo())
                .motivoConsulta(solicitud.getMotivoConsulta())
                .diagnostico(solicitud.getDiagnostico())
                .observaciones(solicitud.getObservaciones())
                .estado("activa")
                .build();
        Consulta consultaGuardada = consultaRepository.save(consulta);

        guardarRecetas(consultaGuardada, solicitud.getRecetas());
        guardarIndicaciones(consultaGuardada, solicitud.getIndicaciones());
        guardarEstudios(consultaGuardada, solicitud.getEstudios());
        guardarAdjuntos(consultaGuardada, solicitud.getAdjuntos());

        return convertirRespuesta(consultaGuardada);
    }

    /** Obtiene una consulta con sus detalles aplicando alcance de historial. */
    @Override
    @Transactional(readOnly = true)
    public ConsultaResponse obtenerConsulta(Long consultaId) {
        Consulta consulta = obtenerEntidad(consultaId);
        validarAlcanceLectura(consulta);
        return convertirRespuesta(consulta);
    }

    /** Lista historial de un paciente. */
    @Override
    @Transactional(readOnly = true)
    public Page<ConsultaResponse> listarPorPaciente(Long pacienteId, Pageable pageable) {
        return consultaRepository.findByPacienteId(pacienteId, pageable).map(this::convertirRespuesta);
    }

    /** Lista historial del medico autenticado. */
    @Override
    @Transactional(readOnly = true)
    public Page<ConsultaResponse> listarPorDoctorAutenticado(Pageable pageable) {
        Usuario usuario = obtenerUsuarioActual();
        return consultaRepository.findByDoctorUsuarioId(usuario.getId(), pageable).map(this::convertirRespuesta);
    }

    /** Agrega una nota de evolucion al historial. */
    @Override
    @Transactional
    public ConsultaResponse agregarNotaEvolucion(Long consultaId, NotaEvolucionRequest solicitud) {
        Consulta consulta = obtenerEntidad(consultaId);
        validarAlcanceLectura(consulta);
        NotaEvolucion nota = NotaEvolucion.builder()
                .consulta(consulta)
                .fecha(LocalDateTime.now())
                .nota(solicitud.getNota())
                .autor(obtenerUsuarioActual())
                .build();
        notaEvolucionRepository.save(nota);
        return convertirRespuesta(consulta);
    }

    /** Guarda fisicamente un adjunto y registra sus metadatos. */
    @Override
    @Transactional
    public AdjuntoResponse agregarAdjunto(Long consultaId, MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new BadRequestException("Debe enviar un archivo adjunto");
        }
        Consulta consulta = obtenerEntidad(consultaId);
        validarAlcanceLectura(consulta);
        String nombreOriginal = StringUtils.cleanPath(archivo.getOriginalFilename() == null
                ? "adjunto"
                : archivo.getOriginalFilename());
        String nombreAlmacenado = UUID.randomUUID() + extension(nombreOriginal);
        try {
            Files.createDirectories(adjuntosStorageDir);
            Path destino = adjuntosStorageDir.resolve(nombreAlmacenado).normalize();
            Files.copy(archivo.getInputStream(), destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException excepcion) {
            throw new BadRequestException("No se pudo guardar el archivo adjunto");
        }
        Adjunto adjunto = adjuntoRepository.save(Adjunto.builder()
                .consulta(consulta)
                .nombreArchivo(nombreOriginal)
                .ruta(nombreAlmacenado)
                .tipoMime(archivo.getContentType() == null ? MediaTypeFallback.APPLICATION_OCTET_STREAM : archivo.getContentType())
                .fechaSubida(LocalDateTime.now())
                .build());
        return mapearAdjunto(adjunto);
    }

    /** Recupera el recurso fisico de un adjunto validando alcance de lectura. */
    @Override
    @Transactional(readOnly = true)
    public AdjuntoDownloadResponse descargarAdjunto(Long adjuntoId) {
        Adjunto adjunto = adjuntoRepository.findById(adjuntoId)
                .orElseThrow(() -> new ResourceNotFoundException("Adjunto no encontrado"));
        validarAlcanceLectura(adjunto.getConsulta());
        try {
            Path ruta = adjuntosStorageDir.resolve(adjunto.getRuta()).normalize();
            Resource resource = new UrlResource(ruta.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("Archivo adjunto no encontrado");
            }
            return new AdjuntoDownloadResponse(adjunto.getNombreArchivo(), adjunto.getTipoMime(), resource);
        } catch (IOException excepcion) {
            throw new ResourceNotFoundException("Archivo adjunto no encontrado");
        }
    }

    /** Guarda recetas asociadas a la consulta. */
    private void guardarRecetas(Consulta consulta, List<RecetaRequest> recetas) {
        if (recetas == null) {
            return;
        }
        recetas.forEach(solicitud -> recetaRepository.save(Receta.builder()
                .consulta(consulta)
                .medicamento(solicitud.getMedicamento())
                .dosis(solicitud.getDosis())
                .frecuencia(solicitud.getFrecuencia())
                .duracion(solicitud.getDuracion())
                .indicaciones(solicitud.getIndicaciones())
                .build()));
    }

    /** Guarda indicaciones medicas validando el tipo permitido. */
    private void guardarIndicaciones(Consulta consulta, List<IndicacionRequest> indicaciones) {
        if (indicaciones == null) {
            return;
        }
        indicaciones.forEach(solicitud -> {
            if (!TIPOS_INDICACION.contains(solicitud.getTipo())) {
                throw new BadRequestException("Tipo de indicacion medica invalido");
            }
            indicacionMedicaRepository.save(IndicacionMedica.builder()
                    .consulta(consulta)
                    .tipo(solicitud.getTipo())
                    .descripcion(solicitud.getDescripcion())
                    .build());
        });
    }

    /** Guarda estudios complementarios en estado pendiente. */
    private void guardarEstudios(Consulta consulta, List<EstudioRequest> estudios) {
        if (estudios == null) {
            return;
        }
        estudios.forEach(solicitud -> estudioComplementarioRepository.save(EstudioComplementario.builder()
                .consulta(consulta)
                .tipoEstudio(solicitud.getTipoEstudio())
                .detalle(solicitud.getDetalle())
                .estado("pendiente")
                .build()));
    }

    /** Guarda referencias de adjuntos ya cargados. */
    private void guardarAdjuntos(Consulta consulta, List<AdjuntoRequest> adjuntos) {
        if (adjuntos == null) {
            return;
        }
        adjuntos.forEach(solicitud -> adjuntoRepository.save(Adjunto.builder()
                .consulta(consulta)
                .nombreArchivo(solicitud.getNombreArchivo())
                .ruta(solicitud.getRuta())
                .tipoMime(solicitud.getTipoMime())
                .fechaSubida(LocalDateTime.now())
                .build()));
    }

    /** Verifica tipo de consulta contra el check constraint de la tabla. */
    private void validarTipoConsulta(String tipo) {
        if (!TIPOS_CONSULTA.contains(tipo)) {
            throw new BadRequestException("Tipo de consulta invalido");
        }
    }

    private String extension(String nombreArchivo) {
        int posicion = nombreArchivo.lastIndexOf('.');
        if (posicion < 0 || posicion == nombreArchivo.length() - 1) {
            return "";
        }
        return nombreArchivo.substring(posicion);
    }

    /** Limita a doctores para que creen solo su propio historial. */
    private void validarAlcanceCreacion(Doctor doctor) {
        Usuario usuario = obtenerUsuarioActual();
        if (tieneAutoridad("ROLE_Doctor") && !doctor.getUsuario().getId().equals(usuario.getId())) {
            throw new AccesoDenegadoException("El doctor solo puede crear consultas propias");
        }
    }

    /** Limita lectura propia si no tiene permiso de ver todos los historiales. */
    private void validarAlcanceLectura(Consulta consulta) {
        Usuario usuario = obtenerUsuarioActual();
        boolean veTodos = tieneAutoridad("historial.ver_todos") || tieneAutoridad("ROLE_Administrador");
        boolean vePropio = tieneAutoridad("historial.ver_propios")
                && consulta.getDoctor().getUsuario().getId().equals(usuario.getId());
        boolean veBasico = tieneAutoridad("historial.ver_basico");
        if (!veTodos && !vePropio && !veBasico) {
            throw new AccesoDenegadoException("No tiene alcance para ver este historial clinico");
        }
    }

    /** Verifica que la cita enviada pertenezca a la misma consulta. */
    private void validarCitaCompatible(Cita cita, Paciente paciente, Doctor doctor, Sede sede) {
        if (cita == null) {
            return;
        }
        boolean compatible = cita.getPaciente().getId().equals(paciente.getId())
                && cita.getDoctor().getId().equals(doctor.getId())
                && cita.getSede().getId().equals(sede.getId());
        if (!compatible) {
            throw new BadRequestException("La cita no corresponde al paciente, doctor y sede enviados");
        }
    }

    /** Recupera consulta o lanza 404. */
    private Consulta obtenerEntidad(Long id) {
        return consultaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consulta no encontrada"));
    }

    /** Recupera paciente o lanza 404. */
    private Paciente obtenerPaciente(Long id) {
        return pacienteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Paciente no encontrado"));
    }

    /** Recupera doctor o lanza 404. */
    private Doctor obtenerDoctor(Long id) {
        return doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor no encontrado"));
    }

    /** Recupera sede o lanza 404. */
    private Sede obtenerSede(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
    }

    /** Recupera cita opcional o lanza 404 si se envio un id inexistente. */
    private Cita obtenerCitaOpcional(Long id) {
        if (id == null) {
            return null;
        }
        return citaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
    }

    /** Obtiene usuario autenticado desde Spring Security. */
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

    /** Convierte consulta y colecciones hijas a DTO. */
    private ConsultaResponse convertirRespuesta(Consulta consulta) {
        return ConsultaResponse.builder()
                .id(consulta.getId())
                .pacienteId(consulta.getPaciente().getId())
                .pacienteNombre(consulta.getPaciente().getNombres() + " " + consulta.getPaciente().getApellidos())
                .doctorId(consulta.getDoctor().getId())
                .doctorNombre(consulta.getDoctor().getUsuario().getNombres() + " "
                        + consulta.getDoctor().getUsuario().getApellidos())
                .sedeId(consulta.getSede().getId())
                .sedeNombre(consulta.getSede().getNombre())
                .citaId(consulta.getCita() == null ? null : consulta.getCita().getId())
                .fechaHora(consulta.getFechaHora())
                .tipo(consulta.getTipo())
                .motivoConsulta(consulta.getMotivoConsulta())
                .diagnostico(consulta.getDiagnostico())
                .observaciones(consulta.getObservaciones())
                .estado(consulta.getEstado())
                .recetas(mapearRecetas(consulta.getId()))
                .indicaciones(mapearIndicaciones(consulta.getId()))
                .estudios(mapearEstudios(consulta.getId()))
                .adjuntos(mapearAdjuntos(consulta.getId()))
                .notasEvolucion(mapearNotas(consulta.getId()))
                .build();
    }

    /** Convierte recetas. */
    private List<RecetaResponse> mapearRecetas(Long consultaId) {
        return recetaRepository.findByConsultaId(consultaId).stream()
                .map(receta -> RecetaResponse.builder()
                        .id(receta.getId())
                        .medicamento(receta.getMedicamento())
                        .dosis(receta.getDosis())
                        .frecuencia(receta.getFrecuencia())
                        .duracion(receta.getDuracion())
                        .indicaciones(receta.getIndicaciones())
                        .build())
                .toList();
    }

    /** Convierte indicaciones. */
    private List<IndicacionResponse> mapearIndicaciones(Long consultaId) {
        return indicacionMedicaRepository.findByConsultaId(consultaId).stream()
                .map(indicacion -> IndicacionResponse.builder()
                        .id(indicacion.getId())
                        .tipo(indicacion.getTipo())
                        .descripcion(indicacion.getDescripcion())
                        .build())
                .toList();
    }

    /** Convierte estudios. */
    private List<EstudioResponse> mapearEstudios(Long consultaId) {
        return estudioComplementarioRepository.findByConsultaId(consultaId).stream()
                .map(estudio -> EstudioResponse.builder()
                        .id(estudio.getId())
                        .tipoEstudio(estudio.getTipoEstudio())
                        .detalle(estudio.getDetalle())
                        .estado(estudio.getEstado())
                        .archivoResultado(estudio.getArchivoResultado())
                        .build())
                .toList();
    }

    /** Convierte adjuntos. */
    private List<AdjuntoResponse> mapearAdjuntos(Long consultaId) {
        return adjuntoRepository.findByConsultaId(consultaId).stream()
                .map(this::mapearAdjunto)
                .toList();
    }

    private AdjuntoResponse mapearAdjunto(Adjunto adjunto) {
        return AdjuntoResponse.builder()
                .id(adjunto.getId())
                .nombreArchivo(adjunto.getNombreArchivo())
                .ruta(adjunto.getRuta())
                .tipoMime(adjunto.getTipoMime())
                .fechaSubida(adjunto.getFechaSubida())
                .build();
    }

    private static class MediaTypeFallback {
        private static final String APPLICATION_OCTET_STREAM = "application/octet-stream";
    }

    /** Convierte notas de evolucion. */
    private List<NotaEvolucionResponse> mapearNotas(Long consultaId) {
        return notaEvolucionRepository.findByConsultaId(consultaId).stream()
                .map(nota -> NotaEvolucionResponse.builder()
                        .id(nota.getId())
                        .fecha(nota.getFecha())
                        .nota(nota.getNota())
                        .autorId(nota.getAutor().getId())
                        .autorNombre(nota.getAutor().getNombres() + " " + nota.getAutor().getApellidos())
                        .build())
                .toList();
    }
}
