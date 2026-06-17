package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.response.PermisoResponse;
import com.web.clinica.model.Permiso;
import com.web.clinica.repository.PermisoRepository;
import com.web.clinica.service.abstractService.IPermisoService;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PermisoServiceImpl implements IPermisoService {

    private final PermisoRepository permisoRepository;

    /** Lista todos los permisos disponibles ordenados por codigo estable. */
    @Override
    @Transactional(readOnly = true)
    public List<PermisoResponse> listar() {
        return permisoRepository.findAll().stream()
                .sorted(Comparator.comparing(Permiso::getCodigo))
                .map(this::convertirRespuesta)
                .toList();
    }

    /** Convierte entidad permiso a DTO. */
    private PermisoResponse convertirRespuesta(Permiso permiso) {
        return PermisoResponse.builder()
                .id(permiso.getId())
                .codigo(permiso.getCodigo())
                .descripcion(permiso.getDescripcion())
                .build();
    }
}
