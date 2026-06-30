package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.ConsultaCreateRequest;
import com.web.clinica.dto.request.NotaEvolucionRequest;
import com.web.clinica.dto.response.AdjuntoDownloadResponse;
import com.web.clinica.dto.response.AdjuntoResponse;
import com.web.clinica.dto.response.ConsultaResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

public interface IHistorialService {

    ConsultaResponse crearConsulta(ConsultaCreateRequest solicitud);

    ConsultaResponse obtenerConsulta(Long consultaId);

    Page<ConsultaResponse> listarPorPaciente(Long pacienteId, Pageable pageable);

    Page<ConsultaResponse> listarPorDoctorAutenticado(Pageable pageable);

    ConsultaResponse agregarNotaEvolucion(Long consultaId, NotaEvolucionRequest solicitud);

    AdjuntoResponse agregarAdjunto(Long consultaId, MultipartFile archivo);

    AdjuntoDownloadResponse descargarAdjunto(Long adjuntoId);

    byte[] generarPdfConsulta(Long consultaId);
}
