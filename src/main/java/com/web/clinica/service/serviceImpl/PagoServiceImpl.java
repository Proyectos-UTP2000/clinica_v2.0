package com.web.clinica.service.serviceImpl;

import com.web.clinica.dto.request.PagoCreateRequest;
import com.web.clinica.dto.response.PagoResponse;
import com.web.clinica.exception.AccesoDenegadoException;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Cita;
import com.web.clinica.model.Pago;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.CitaRepository;
import com.web.clinica.repository.PagoRepository;
import com.web.clinica.service.abstractService.IPagoService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PagoServiceImpl implements IPagoService {

    private static final List<String> METODOS_PERMITIDOS = List.of("efectivo", "tarjeta", "transferencia", "web");

    private final PagoRepository pagoRepository;
    private final CitaRepository citaRepository;

    /** Registra un pago unico y marca la cita como pagada. */
    @Override
    @Transactional
    public PagoResponse registrarPago(PagoCreateRequest solicitud) {
        validarMetodo(solicitud.getMetodo());
        Cita cita = citaRepository.findById(solicitud.getCitaId())
                .orElseThrow(() -> new ResourceNotFoundException("Cita no encontrada"));
        if ("pagado".equals(cita.getEstadoPago()) || pagoRepository.findByCitaId(cita.getId()).isPresent()) {
            throw new BadRequestException("La cita ya tiene un pago registrado");
        }

        Pago pago = Pago.builder()
                .cita(cita)
                .monto(solicitud.getMonto())
                .metodo(solicitud.getMetodo())
                .fechaPago(LocalDateTime.now())
                .registradoPorUsuario(obtenerUsuarioActual())
                .build();
        cita.setEstadoPago("pagado");
        citaRepository.save(cita);
        return convertirRespuesta(pagoRepository.save(pago));
    }

    /** Obtiene pago por cita. */
    @Override
    @Transactional(readOnly = true)
    public PagoResponse obtenerPorCita(Long citaId) {
        return pagoRepository.findByCitaId(citaId)
                .map(this::convertirRespuesta)
                .orElseThrow(() -> new ResourceNotFoundException("Pago no encontrado"));
    }

    /** Lista pagos asociados a un paciente. */
    @Override
    @Transactional(readOnly = true)
    public List<PagoResponse> listarPorPaciente(Long pacienteId) {
        return pagoRepository.findByCitaPacienteId(pacienteId).stream()
                .map(this::convertirRespuesta)
                .toList();
    }

    /** Valida metodo contra el check constraint de la tabla. */
    private void validarMetodo(String metodo) {
        if (!METODOS_PERMITIDOS.contains(metodo)) {
            throw new BadRequestException("Metodo de pago invalido");
        }
    }

    /** Obtiene usuario autenticado para auditoria del pago. */
    private Usuario obtenerUsuarioActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        throw new AccesoDenegadoException("No se pudo resolver el usuario autenticado");
    }

    /** Convierte entidad pago a DTO de respuesta. */
    private PagoResponse convertirRespuesta(Pago pago) {
        Usuario usuario = pago.getRegistradoPorUsuario();
        return PagoResponse.builder()
                .id(pago.getId())
                .citaId(pago.getCita().getId())
                .monto(pago.getMonto())
                .metodo(pago.getMetodo())
                .fechaPago(pago.getFechaPago())
                .registradoPorId(usuario == null ? null : usuario.getId())
                .registradoPor(usuario == null ? null : usuario.getNombres() + " " + usuario.getApellidos())
                .build();
    }
}
