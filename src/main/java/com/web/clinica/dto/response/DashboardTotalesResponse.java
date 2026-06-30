package com.web.clinica.dto.response;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DashboardTotalesResponse {

    private long totalPacientes;
    private long totalMedicos;
    private long totalCitasProgramadas;
    private long citasHoy;
    private long citasAtendidas;
    private long citasCanceladas;
    private long citasNoAsistidas;
    private long citasReprogramadas;
}
