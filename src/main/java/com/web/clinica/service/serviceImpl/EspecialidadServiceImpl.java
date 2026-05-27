package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.EspecialidadCreateRequest;
import com.web.clinica.dto.request.EspecialidadUpdateRequest;
import com.web.clinica.dto.response.EspecialidadResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Especialidad;
import com.web.clinica.repository.EspecialidadRepository;
import com.web.clinica.service.abstractService.IEspecialidadService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class EspecialidadServiceImpl implements IEspecialidadService {

    private final EspecialidadRepository especialidadRepository;

    /** Crea una especialidad validando nombre unico. */
    @Override
    @Transactional
    public EspecialidadResponse crear(EspecialidadCreateRequest solicitud) {
        validarNombreDisponible(solicitud.getNombre(), null);
        Especialidad especialidad = Especialidad.builder()
                .nombre(solicitud.getNombre())
                .descripcion(solicitud.getDescripcion())
                .especialidadPadre(obtenerPadreOpcional(solicitud.getEspecialidadPadreId()))
                .build();
        return convertirRespuesta(especialidadRepository.save(especialidad));
    }

    /** Actualiza una especialidad y valida que no sea padre de si misma. */
    @Override
    @Transactional
    public EspecialidadResponse actualizar(Long id, EspecialidadUpdateRequest solicitud) {
        Especialidad especialidad = obtenerEntidad(id);
        validarNombreDisponible(solicitud.getNombre(), id);
        if (solicitud.getEspecialidadPadreId() != null && solicitud.getEspecialidadPadreId().equals(id)) {
            throw new BadRequestException("La especialidad no puede ser padre de si misma");
        }
        especialidad.setNombre(solicitud.getNombre());
        especialidad.setDescripcion(solicitud.getDescripcion());
        especialidad.setEspecialidadPadre(obtenerPadreOpcional(solicitud.getEspecialidadPadreId()));
        return convertirRespuesta(especialidadRepository.save(especialidad));
    }

    /** Obtiene especialidad por id. */
    @Override
    @Transactional(readOnly = true)
    public EspecialidadResponse obtenerPorId(Long id) {
        return convertirRespuesta(obtenerEntidad(id));
    }

    /** Lista especialidades con paginacion. */
    @Override
    @Transactional(readOnly = true)
    public Page<EspecialidadResponse> listar(Pageable pageable) {
        return especialidadRepository.findAllByOrderByNombreAsc(pageable).map(this::convertirRespuesta);
    }

    /** Lista todas las especialidades ordenadas por nombre. */
    @Override
    @Transactional(readOnly = true)
    public List<EspecialidadResponse> listarTodas() {
        return especialidadRepository.findAllByOrderByNombreAsc().stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    /** Elimina especialidad fisicamente porque la tabla no tiene columna activo. */
    @Override
    @Transactional
    public void eliminar(Long id) {
        Especialidad especialidad = obtenerEntidad(id);
        especialidadRepository.delete(especialidad);
    }

    /** Valida que el nombre no este tomado por otra especialidad. */
    private void validarNombreDisponible(String nombre, Long idActual) {
        boolean duplicado = idActual == null
                ? especialidadRepository.findByNombre(nombre).isPresent()
                : especialidadRepository.existsByNombreAndIdNot(nombre, idActual);
        if (duplicado) {
            throw new BadRequestException("Ya existe una especialidad con ese nombre");
        }
    }

    /** Devuelve el padre opcional o null si no fue enviado. */
    private Especialidad obtenerPadreOpcional(Long especialidadPadreId) {
        if (especialidadPadreId == null) {
            return null;
        }
        return obtenerEntidad(especialidadPadreId);
    }

    /** Recupera especialidad o lanza 404. */
    private Especialidad obtenerEntidad(Long id) {
        return especialidadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Especialidad no encontrada"));
    }

    /** Convierte entidad a DTO de respuesta. */
    private EspecialidadResponse convertirRespuesta(Especialidad especialidad) {
        Especialidad padre = especialidad.getEspecialidadPadre();
        return EspecialidadResponse.builder()
                .id(especialidad.getId())
                .nombre(especialidad.getNombre())
                .descripcion(especialidad.getDescripcion())
                .especialidadPadreId(padre == null ? null : padre.getId())
                .especialidadPadreNombre(padre == null ? null : padre.getNombre())
                .build();
    }
}
