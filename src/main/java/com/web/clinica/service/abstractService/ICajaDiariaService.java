package com.web.clinica.service.abstractService;

import com.web.clinica.dto.request.CajaDiariaRequest;
import com.web.clinica.dto.request.CierreCajaRequest;
import com.web.clinica.dto.response.CajaDiariaResponse;
import java.time.LocalDate;

public interface ICajaDiariaService {

    CajaDiariaResponse abrirCaja(CajaDiariaRequest solicitud);

    CajaDiariaResponse cerrarCaja(CierreCajaRequest solicitud);

    CajaDiariaResponse obtenerCajaDelDia();

    CajaDiariaResponse obtenerCajaPorFecha(LocalDate fecha);

    byte[] generarReportePdf(Long cajaId);
}
