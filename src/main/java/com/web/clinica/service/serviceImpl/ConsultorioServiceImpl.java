package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.ConsultorioCreateRequest;
import com.web.clinica.dto.request.ConsultorioUpdateRequest;
import com.web.clinica.dto.response.ConsultorioResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Consultorio;
import com.web.clinica.model.Sede;
import com.web.clinica.repository.ConsultorioRepository;
import com.web.clinica.repository.SedeRepository;
import com.web.clinica.service.abstractService.IConsultorioService;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ConsultorioServiceImpl implements IConsultorioService {

    private final ConsultorioRepository consultorioRepository;
    private final SedeRepository sedeRepository;

    @Override
    @Transactional
    public ConsultorioResponse crear(ConsultorioCreateRequest solicitud) {
        Sede sede = sedeRepository.findById(solicitud.getSedeId())
                .orElseThrow(() -> new ResourceNotFoundException("Sede no encontrada"));

        consultorioRepository.findBySedeIdAndNombreIgnoreCase(solicitud.getSedeId(), solicitud.getNombre())
                .ifPresent(c -> {
                    throw new BadRequestException("Ya existe un consultorio con ese nombre en la sede");
                });

        Consultorio consultorio = Consultorio.builder()
                .sede(sede)
                .nombre(solicitud.getNombre())
                .piso(solicitud.getPiso())
                .area(solicitud.getArea())
                .activo(true)
                .build();

        return convertirRespuesta(consultorioRepository.save(consultorio));
    }

    @Override
    @Transactional
    public ConsultorioResponse actualizar(Long id, ConsultorioUpdateRequest solicitud) {
        Consultorio consultorio = obtenerEntidad(id);

        if (!consultorio.getNombre().equalsIgnoreCase(solicitud.getNombre())) {
            consultorioRepository.findBySedeIdAndNombreIgnoreCase(consultorio.getSede().getId(), solicitud.getNombre())
                    .ifPresent(c -> {
                        throw new BadRequestException("Ya existe un consultorio con ese nombre en la sede");
                    });
        }

        consultorio.setNombre(solicitud.getNombre());
        consultorio.setPiso(solicitud.getPiso());
        consultorio.setArea(solicitud.getArea());

        return convertirRespuesta(consultorioRepository.save(consultorio));
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultorioResponse obtenerPorId(Long id) {
        return convertirRespuesta(obtenerEntidad(id));
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ConsultorioResponse> listarActivos(Pageable pageable) {
        return consultorioRepository.findByActivoTrue(pageable).map(this::convertirRespuesta);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ConsultorioResponse> listarPorSede(Long sedeId) {
        return consultorioRepository.findBySedeIdAndActivoTrue(sedeId).stream()
                .map(this::convertirRespuesta)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void desactivar(Long id) {
        Consultorio consultorio = obtenerEntidad(id);
        consultorio.setActivo(false);
        consultorioRepository.save(consultorio);
    }

    private Consultorio obtenerEntidad(Long id) {
        return consultorioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Consultorio no encontrado"));
    }

    private ConsultorioResponse convertirRespuesta(Consultorio consultorio) {
        return ConsultorioResponse.builder()
                .id(consultorio.getId())
                .sedeId(consultorio.getSede().getId())
                .sedeNombre(consultorio.getSede().getNombre())
                .nombre(consultorio.getNombre())
                .piso(consultorio.getPiso())
                .area(consultorio.getArea())
                .activo(consultorio.getActivo())
                .build();
    }
}
