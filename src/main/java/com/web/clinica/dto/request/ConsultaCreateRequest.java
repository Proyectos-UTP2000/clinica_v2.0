package com.web.clinica.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;
import lombok.Data;

@Data
public class ConsultaCreateRequest {

    @NotNull
    private Long pacienteId;

    @NotNull
    private Long doctorId;

    @NotNull
    private Long sedeId;

    private Long citaId;

    @NotBlank
    private String tipo;

    private String motivoConsulta;
    private String diagnostico;
    private String observaciones;

    @Valid
    private List<RecetaRequest> recetas = new ArrayList<>();

    @Valid
    private List<IndicacionRequest> indicaciones = new ArrayList<>();

    @Valid
    private List<EstudioRequest> estudios = new ArrayList<>();

    @Valid
    private List<AdjuntoRequest> adjuntos = new ArrayList<>();
}
