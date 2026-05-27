package com.web.clinica.dto.response;

import java.time.LocalDateTime;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ConsultaResponse {

    private Long id;
    private Long pacienteId;
    private String pacienteNombre;
    private Long doctorId;
    private String doctorNombre;
    private Long sedeId;
    private String sedeNombre;
    private Long citaId;
    private LocalDateTime fechaHora;
    private String tipo;
    private String motivoConsulta;
    private String diagnostico;
    private String observaciones;
    private String estado;
    private List<RecetaResponse> recetas;
    private List<IndicacionResponse> indicaciones;
    private List<EstudioResponse> estudios;
    private List<AdjuntoResponse> adjuntos;
    private List<NotaEvolucionResponse> notasEvolucion;
}
