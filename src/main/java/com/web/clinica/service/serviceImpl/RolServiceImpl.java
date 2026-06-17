package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.AsignarPermisosRequest;
import com.web.clinica.dto.request.RolCreateRequest;
import com.web.clinica.dto.request.RolUpdateRequest;
import com.web.clinica.dto.response.PermisoResponse;
import com.web.clinica.dto.response.RolResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Permiso;
import com.web.clinica.model.Rol;
import com.web.clinica.repository.PermisoRepository;
import com.web.clinica.repository.RolRepository;
import com.web.clinica.service.abstractService.IRolService;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RolServiceImpl implements IRolService {

    private final RolRepository rolRepository;
    private final PermisoRepository permisoRepository;

    /** Lista roles con su estado para administracion. */
    @Override
    @Transactional(readOnly = true)
    public Page<RolResponse> listar(Pageable pageable) {
        return rolRepository.findAll(pageable).map(this::convertirRespuesta);
    }

    /** Obtiene un rol por id. */
    @Override
    @Transactional(readOnly = true)
    public RolResponse obtenerPorId(Long id) {
        return convertirRespuesta(obtenerEntidad(id));
    }

    /** Crea un rol activo y asigna permisos iniciales. */
    @Override
    @Transactional
    public RolResponse crear(RolCreateRequest solicitud) {
        validarNombreDisponible(solicitud.getNombre(), null);
        Rol rol = new Rol();
        rol.setNombre(solicitud.getNombre());
        rol.setDescripcion(solicitud.getDescripcion());
        rol.setActivo(true);
        rol.setPermisos(cargarPermisos(solicitud.getPermisosIds()));
        return convertirRespuesta(rolRepository.save(rol));
    }

    /** Actualiza datos basicos y checklist de permisos de un rol. */
    @Override
    @Transactional
    public RolResponse actualizar(Long id, RolUpdateRequest solicitud) {
        Rol rol = obtenerEntidad(id);
        validarNombreDisponible(solicitud.getNombre(), id);
        rol.setNombre(solicitud.getNombre());
        rol.setDescripcion(solicitud.getDescripcion());
        if (solicitud.getActivo() != null) {
            rol.setActivo(solicitud.getActivo());
        }
        rol.setPermisos(cargarPermisos(solicitud.getPermisosIds()));
        return convertirRespuesta(rolRepository.save(rol));
    }

    /** Reemplaza permisos asignados por el checklist enviado. */
    @Override
    @Transactional
    public RolResponse asignarPermisos(Long id, AsignarPermisosRequest solicitud) {
        Rol rol = obtenerEntidad(id);
        rol.setPermisos(cargarPermisos(solicitud.getPermisosIds()));
        return convertirRespuesta(rolRepository.save(rol));
    }

    /** Desactiva un rol sin eliminarlo fisicamente. */
    @Override
    @Transactional
    public void desactivar(Long id) {
        Rol rol = obtenerEntidad(id);
        rol.setActivo(false);
        rolRepository.save(rol);
    }

    /** Recupera rol o lanza 404. */
    private Rol obtenerEntidad(Long id) {
        return rolRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado"));
    }

    /** Valida que no exista otro rol con el mismo nombre. */
    private void validarNombreDisponible(String nombre, Long idActual) {
        rolRepository.findByNombre(nombre)
                .filter(rol -> idActual == null || !rol.getId().equals(idActual))
                .ifPresent(rol -> {
                    throw new BadRequestException("Ya existe un rol con ese nombre");
                });
    }

    /** Carga y valida todos los permisos solicitados. */
    private Set<Permiso> cargarPermisos(List<Long> permisosIds) {
        List<Long> ids = permisosIds == null ? List.of() : permisosIds;
        if (ids.isEmpty()) {
            return new HashSet<>();
        }
        List<Permiso> permisos = permisoRepository.findAllById(ids);
        if (permisos.size() != Set.copyOf(ids).size()) {
            throw new BadRequestException("Permisos no encontrados");
        }
        return new HashSet<>(permisos);
    }

    /** Convierte entidad rol a DTO de respuesta. */
    private RolResponse convertirRespuesta(Rol rol) {
        return RolResponse.builder()
                .id(rol.getId())
                .nombre(rol.getNombre())
                .descripcion(rol.getDescripcion())
                .activo(rol.getActivo())
                .permisos(rol.getPermisos().stream()
                        .sorted(Comparator.comparing(Permiso::getCodigo))
                        .map(this::convertirPermiso)
                        .toList())
                .build();
    }

    /** Convierte permiso a DTO anidado. */
    private PermisoResponse convertirPermiso(Permiso permiso) {
        return PermisoResponse.builder()
                .id(permiso.getId())
                .codigo(permiso.getCodigo())
                .descripcion(permiso.getDescripcion())
                .build();
    }
}
