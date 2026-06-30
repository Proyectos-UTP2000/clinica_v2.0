package com.web.clinica.service.serviceImpl;

import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;
import com.web.clinica.dto.request.CajaDiariaRequest;
import com.web.clinica.dto.request.CierreCajaRequest;
import com.web.clinica.dto.response.CajaDiariaResponse;
import com.web.clinica.exception.AccesoDenegadoException;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.CajaDiaria;
import com.web.clinica.model.Pago;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.CajaDiariaRepository;
import com.web.clinica.repository.PagoRepository;
import com.web.clinica.service.abstractService.ICajaDiariaService;
import java.io.ByteArrayOutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CajaDiariaServiceImpl implements ICajaDiariaService {

    private final CajaDiariaRepository cajaDiariaRepository;
    private final PagoRepository pagoRepository;

    @Override
    @Transactional
    public CajaDiariaResponse abrirCaja(CajaDiariaRequest solicitud) {
        LocalDate hoy = LocalDate.now();
        if (cajaDiariaRepository.findByFecha(hoy).isPresent()) {
            throw new BadRequestException("Ya existe un registro de caja para el día de hoy.");
        }

        CajaDiaria caja = CajaDiaria.builder()
                .fecha(hoy)
                .montoApertura(solicitud.getMontoApertura())
                .estado("abierta")
                .fechaApertura(LocalDateTime.now())
                .abiertoPorUsuario(obtenerUsuarioActual())
                .observaciones(solicitud.getObservaciones())
                .build();

        return convertirRespuesta(cajaDiariaRepository.save(caja));
    }

    @Override
    @Transactional
    public CajaDiariaResponse cerrarCaja(CierreCajaRequest solicitud) {
        LocalDate hoy = LocalDate.now();
        CajaDiaria caja = cajaDiariaRepository.findByFechaAndEstado(hoy, "abierta")
                .orElseThrow(() -> new BadRequestException("No hay ninguna caja abierta para el día de hoy."));

        // Sumar todos los pagos asociados a esta caja.
        // Los ingresos de caja se actualizan dinámicamente cuando se registran los pagos.
        // Pero por seguridad recalculamos y actualizamos.
        List<Pago> pagos = pagoRepository.findByCajaDiariaId(caja.getId());
        BigDecimal totalIngresos = pagos.stream()
                .map(Pago::getMonto)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        caja.setIngresos(totalIngresos);
        caja.setMontoCierre(caja.getMontoApertura().add(totalIngresos).subtract(caja.getEgresos()));
        caja.setBalanceReal(solicitud.getBalanceReal());
        caja.setDiferencia(solicitud.getBalanceReal().subtract(caja.getMontoCierre()));
        caja.setEstado("cerrada");
        caja.setFechaCierre(LocalDateTime.now());
        caja.setCerradoPorUsuario(obtenerUsuarioActual());
        if (solicitud.getObservaciones() != null && !solicitud.getObservaciones().isEmpty()) {
            String obs = caja.getObservaciones() != null ? caja.getObservaciones() + "\n" + solicitud.getObservaciones() : solicitud.getObservaciones();
            caja.setObservaciones(obs);
        }

        return convertirRespuesta(cajaDiariaRepository.save(caja));
    }

    @Override
    @Transactional(readOnly = true)
    public CajaDiariaResponse obtenerCajaDelDia() {
        LocalDate hoy = LocalDate.now();
        return cajaDiariaRepository.findByFecha(hoy)
                .map(this::convertirRespuesta)
                .orElseThrow(() -> new ResourceNotFoundException("No se ha registrado apertura de caja para el día de hoy."));
    }

    @Override
    @Transactional(readOnly = true)
    public CajaDiariaResponse obtenerCajaPorFecha(LocalDate fecha) {
        return cajaDiariaRepository.findByFecha(fecha)
                .map(this::convertirRespuesta)
                .orElseThrow(() -> new ResourceNotFoundException("No existe registro de caja para la fecha especificada."));
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] generarReportePdf(Long cajaId) {
        CajaDiaria caja = cajaDiariaRepository.findById(cajaId)
                .orElseThrow(() -> new ResourceNotFoundException("Caja diaria no encontrada"));

        List<Pago> pagos = pagoRepository.findByCajaDiariaId(caja.getId());

        Document document = new Document(PageSize.A4);
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            PdfWriter.getInstance(document, out);
            document.open();

            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Font boldFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);

            Paragraph title = new Paragraph("REPORTE DE CIERRE DE CAJA DIARIA", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(new Paragraph(" "));

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

            document.add(new Paragraph("Información General", sectionFont));
            document.add(new Paragraph("Fecha de Caja: " + caja.getFecha().toString(), boldFont));
            document.add(new Paragraph("Estado: " + caja.getEstado().toUpperCase(), normalFont));
            document.add(new Paragraph("Fecha Apertura: " + (caja.getFechaApertura() != null ? dtf.format(caja.getFechaApertura()) : "-"), normalFont));
            document.add(new Paragraph("Abierto por: " + (caja.getAbiertoPorUsuario() != null ? caja.getAbiertoPorUsuario().getNombres() + " " + caja.getAbiertoPorUsuario().getApellidos() : "Sistema"), normalFont));
            if ("cerrada".equals(caja.getEstado())) {
                document.add(new Paragraph("Fecha Cierre: " + (caja.getFechaCierre() != null ? dtf.format(caja.getFechaCierre()) : "-"), normalFont));
                document.add(new Paragraph("Cerrado por: " + (caja.getCerradoPorUsuario() != null ? caja.getCerradoPorUsuario().getNombres() + " " + caja.getCerradoPorUsuario().getApellidos() : "Sistema"), normalFont));
            }
            document.add(new Paragraph(" "));

            document.add(new Paragraph("Balance Económico", sectionFont));
            document.add(new Paragraph("Monto Apertura: S/. " + caja.getMontoApertura(), normalFont));
            document.add(new Paragraph("Ingresos Totales: S/. " + caja.getIngresos(), normalFont));
            document.add(new Paragraph("Egresos Totales: S/. " + caja.getEgresos(), normalFont));
            
            BigDecimal finalCalculado = caja.getMontoApertura().add(caja.getIngresos()).subtract(caja.getEgresos());
            document.add(new Paragraph("Monto Final Esperado: S/. " + finalCalculado, boldFont));

            if ("cerrada".equals(caja.getEstado())) {
                document.add(new Paragraph("Monto Físico/Real Reportado: S/. " + caja.getBalanceReal(), boldFont));
                document.add(new Paragraph("Diferencia (Sobrante/Faltante): S/. " + caja.getDiferencia(), boldFont));
            }
            document.add(new Paragraph(" "));

            if (caja.getObservaciones() != null && !caja.getObservaciones().isEmpty()) {
                document.add(new Paragraph("Observaciones", sectionFont));
                document.add(new Paragraph(caja.getObservaciones(), normalFont));
                document.add(new Paragraph(" "));
            }

            document.add(new Paragraph("Desglose de Transacciones (Cobros)", sectionFont));
            document.add(new Paragraph(" "));

            PdfPTable table = new PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[]{2f, 4f, 2f, 2f});

            table.addCell(new PdfPCell(new Phrase("Fecha/Hora", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Paciente", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Método", boldFont)));
            table.addCell(new PdfPCell(new Phrase("Monto", boldFont)));

            for (Pago pago : pagos) {
                table.addCell(new PdfPCell(new Phrase(dtf.format(pago.getFechaPago()), normalFont)));
                table.addCell(new PdfPCell(new Phrase(pago.getCita().getPaciente().getNombres() + " " + pago.getCita().getPaciente().getApellidos(), normalFont)));
                table.addCell(new PdfPCell(new Phrase(pago.getMetodo().toUpperCase(), normalFont)));
                table.addCell(new PdfPCell(new Phrase("S/. " + pago.getMonto(), normalFont)));
            }

            document.add(table);
            document.close();
        } catch (Exception e) {
            throw new RuntimeException("Error al generar PDF de reporte de caja", e);
        }

        return out.toByteArray();
    }

    private Usuario obtenerUsuarioActual() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof Usuario usuario) {
            return usuario;
        }
        throw new AccesoDenegadoException("No se pudo resolver el usuario autenticado");
    }

    @Override
    @Transactional
    public CajaDiariaResponse reabrirCaja() {
        LocalDate hoy = LocalDate.now();
        CajaDiaria caja = cajaDiariaRepository.findByFechaAndEstado(hoy, "cerrada")
                .orElseThrow(() -> new BadRequestException("No hay ninguna caja cerrada hoy para reabrir."));

        caja.setEstado("abierta");
        caja.setFechaCierre(null);
        caja.setCerradoPorUsuario(null);
        caja.setMontoCierre(null);
        caja.setDiferencia(null);
        caja.setBalanceReal(null);

        return convertirRespuesta(cajaDiariaRepository.save(caja));
    }

    private CajaDiariaResponse convertirRespuesta(CajaDiaria caja) {
        return CajaDiariaResponse.builder()
                .id(caja.getId())
                .fecha(caja.getFecha())
                .montoApertura(caja.getMontoApertura())
                .montoCierre(caja.getMontoCierre())
                .ingresos(caja.getIngresos())
                .egresos(caja.getEgresos())
                .balanceReal(caja.getBalanceReal())
                .diferencia(caja.getDiferencia())
                .estado(caja.getEstado())
                .fechaApertura(caja.getFechaApertura())
                .fechaCierre(caja.getFechaCierre())
                .abiertoPorNombre(caja.getAbiertoPorUsuario() != null ? caja.getAbiertoPorUsuario().getNombres() + " " + caja.getAbiertoPorUsuario().getApellidos() : null)
                .cerradoPorNombre(caja.getCerradoPorUsuario() != null ? caja.getCerradoPorUsuario().getNombres() + " " + caja.getCerradoPorUsuario().getApellidos() : null)
                .observaciones(caja.getObservaciones())
                .build();
    }
}
