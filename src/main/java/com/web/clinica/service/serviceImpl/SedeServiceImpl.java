package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.SedeCreateRequest;
import com.web.clinica.dto.request.SedeUpdateRequest;
import com.web.clinica.dto.response.SedeResponse;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Sede;
import com.web.clinica.repository.SedeRepository;
import com.web.clinica.service.abstractService.ISedeService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SedeServiceImpl implements ISedeService {

    private final SedeRepository sedeRepository;

    /** Crea una sede activa. */
    @Override
    @Transactional
    public SedeResponse crear(SedeCreateRequest solicitud) {
        Sede sede = Sede.builder()
                .nombre(solicitud.getNombre())
                .direccion(solicitud.getDireccion())
                .activo(true)
                .build();
        return convertirRespuesta(sedeRepository.save(sede));
    }

    /** Actualiza los datos basicos de una sede. */
    @Override
    @Transactional
    public SedeResponse actualizar(Long id, SedeUpdateRequest solicitud) {
        Sede sede = obtenerEntidad(id);
        sede.setNombre(solicitud.getNombre());
        sede.setDireccion(solicitud.getDireccion());
        return convertirRespuesta(sedeRepository.save(sede));
    }

    /** Obtiene una sede por identificador. */
    @Override
    @Transactional(readOnly = true)
    public SedeResponse obtenerPorId(Long id) {
        return convertirRespuesta(obtenerEntidad(id));
    }

    /** Lista sedes activas con paginacion. */
    @Override
    @Transactional(readOnly = true)
    public Page<SedeResponse> listarActivos(Pageable pageable) {
        return sedeRepository.findByActivoTrue(pageable).map(this::convertirRespuesta);
    }

    /** Desactiva una sede sin eliminarla fisicamente. */
    @Override
    @Transactional
    public void desactivar(Long id) {
        Sede sede = obtenerEntidad(id);
        sede.setActivo(false);
        sedeRepository.save(sede);
    }

    /** Recupera sede o lanza 404. */
    private Sede obtenerEntidad(Long id) {
        return sedeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));
    }

    /** Convierte entidad sede a DTO de respuesta. */
    private SedeResponse convertirRespuesta(Sede sede) {
        return SedeResponse.builder()
                .id(sede.getId())
                .nombre(sede.getNombre())
                .direccion(sede.getDireccion())
                .activo(sede.getActivo())
                .build();
    }
}
