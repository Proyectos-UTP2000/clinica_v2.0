package com.web.clinica;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.clinica.controller.ConsultaController;
import com.web.clinica.controller.AdjuntoController;
import com.web.clinica.controller.DashboardController;
import com.web.clinica.controller.EspecialidadController;
import com.web.clinica.controller.PagoController;
import com.web.clinica.controller.SedeController;
import com.web.clinica.dto.request.ConsultaCreateRequest;
import com.web.clinica.dto.request.EspecialidadCreateRequest;
import com.web.clinica.dto.request.EspecialidadUpdateRequest;
import com.web.clinica.dto.request.NotaEvolucionRequest;
import com.web.clinica.dto.request.PagoCreateRequest;
import com.web.clinica.dto.request.SedeCreateRequest;
import com.web.clinica.dto.request.SedeUpdateRequest;
import com.web.clinica.dto.response.ConsultaResponse;
import com.web.clinica.dto.response.DashboardTotalesResponse;
import com.web.clinica.dto.response.EspecialidadResponse;
import com.web.clinica.dto.response.PagoResponse;
import com.web.clinica.dto.response.SedeResponse;
import com.web.clinica.dto.response.AdjuntoDownloadResponse;
import com.web.clinica.dto.response.AdjuntoResponse;
import com.web.clinica.service.abstractService.IDashboardService;
import com.web.clinica.service.abstractService.IEspecialidadService;
import com.web.clinica.service.abstractService.IHistorialService;
import com.web.clinica.service.abstractService.IPagoService;
import com.web.clinica.service.abstractService.ISedeService;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class Prompt4ControllerSmokeTests {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private MockMvc mockMvc;

    /** Construye controladores reales con servicios stub para validar rutas. */
    @BeforeEach
    void configurarMockMvc() {
        mockMvc = MockMvcBuilders.standaloneSetup(
                new SedeController(new SedeServiceStub()),
                new EspecialidadController(new EspecialidadServiceStub()),
                new ConsultaController(new HistorialServiceStub()),
                new AdjuntoController(new HistorialServiceStub()),
                new PagoController(new PagoServiceStub()),
                new DashboardController(new DashboardServiceStub())
        ).setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();
    }

    /** Verifica endpoints de sedes. */
    @Test
    void endpointsSedesResponden() throws Exception {
        mockMvc.perform(get("/api/sedes")).andExpect(status().isOk());
        mockMvc.perform(get("/api/sedes/1")).andExpect(status().isOk());
        mockMvc.perform(post("/api/sedes").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sedeCrear()))).andExpect(status().isCreated());
        mockMvc.perform(put("/api/sedes/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sedeActualizar()))).andExpect(status().isOk());
        mockMvc.perform(delete("/api/sedes/1")).andExpect(status().isOk());
    }

    /** Verifica endpoints de especialidades. */
    @Test
    void endpointsEspecialidadesResponden() throws Exception {
        mockMvc.perform(get("/api/especialidades")).andExpect(status().isOk());
        mockMvc.perform(get("/api/especialidades/todas")).andExpect(status().isOk());
        mockMvc.perform(get("/api/especialidades/1")).andExpect(status().isOk());
        mockMvc.perform(post("/api/especialidades").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(especialidadCrear()))).andExpect(status().isCreated());
        mockMvc.perform(put("/api/especialidades/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(especialidadActualizar()))).andExpect(status().isOk());
        mockMvc.perform(delete("/api/especialidades/1")).andExpect(status().isOk());
    }

    /** Verifica endpoints de historial clinico. */
    @Test
    void endpointsHistorialResponden() throws Exception {
        mockMvc.perform(post("/api/consultas").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(consultaCrear()))).andExpect(status().isCreated());
        mockMvc.perform(get("/api/consultas/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/consultas/paciente/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/consultas/doctor")).andExpect(status().isOk());
        mockMvc.perform(post("/api/consultas/1/notas").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(notaCrear()))).andExpect(status().isOk());
        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "resultado.pdf",
                MediaType.APPLICATION_PDF_VALUE,
                "PDF".getBytes());
        mockMvc.perform(multipart("/api/consultas/1/adjuntos").file(archivo)).andExpect(status().isCreated());
        mockMvc.perform(get("/api/adjuntos/1")).andExpect(status().isOk());
    }

    /** Verifica endpoints de pagos. */
    @Test
    void endpointsPagosResponden() throws Exception {
        mockMvc.perform(post("/api/pagos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(pagoCrear()))).andExpect(status().isCreated());
        mockMvc.perform(get("/api/pagos/cita/1")).andExpect(status().isOk());
        mockMvc.perform(get("/api/pagos/paciente/1")).andExpect(status().isOk());
    }

    /** Verifica endpoint de dashboard. */
    @Test
    void endpointDashboardResponde() throws Exception {
        mockMvc.perform(get("/api/dashboard/totales")).andExpect(status().isOk());
    }

    private SedeCreateRequest sedeCrear() {
        SedeCreateRequest solicitud = new SedeCreateRequest();
        solicitud.setNombre("Central");
        solicitud.setDireccion("Av. Principal 123");
        return solicitud;
    }

    private SedeUpdateRequest sedeActualizar() {
        SedeUpdateRequest solicitud = new SedeUpdateRequest();
        solicitud.setNombre("Central Actualizada");
        solicitud.setDireccion("Av. Principal 456");
        return solicitud;
    }

    private EspecialidadCreateRequest especialidadCrear() {
        EspecialidadCreateRequest solicitud = new EspecialidadCreateRequest();
        solicitud.setNombre("Cardiologia");
        solicitud.setDescripcion("Especialidad cardiovascular");
        return solicitud;
    }

    private EspecialidadUpdateRequest especialidadActualizar() {
        EspecialidadUpdateRequest solicitud = new EspecialidadUpdateRequest();
        solicitud.setNombre("Cardiologia Clinica");
        solicitud.setDescripcion("Especialidad cardiovascular");
        return solicitud;
    }

    private ConsultaCreateRequest consultaCrear() {
        ConsultaCreateRequest solicitud = new ConsultaCreateRequest();
        solicitud.setPacienteId(1L);
        solicitud.setDoctorId(1L);
        solicitud.setSedeId(1L);
        solicitud.setTipo("consulta");
        return solicitud;
    }

    private NotaEvolucionRequest notaCrear() {
        NotaEvolucionRequest solicitud = new NotaEvolucionRequest();
        solicitud.setNota("Paciente evoluciona favorablemente");
        return solicitud;
    }

    private PagoCreateRequest pagoCrear() {
        PagoCreateRequest solicitud = new PagoCreateRequest();
        solicitud.setCitaId(1L);
        solicitud.setMonto(BigDecimal.TEN);
        solicitud.setMetodo("efectivo");
        return solicitud;
    }

    private static class SedeServiceStub implements ISedeService {

        @Override
        public SedeResponse crear(SedeCreateRequest solicitud) {
            return respuesta();
        }

        @Override
        public SedeResponse actualizar(Long id, SedeUpdateRequest solicitud) {
            return respuesta();
        }

        @Override
        public SedeResponse obtenerPorId(Long id) {
            return respuesta();
        }

        @Override
        public Page<SedeResponse> listarActivos(Pageable pageable) {
            return new PageImpl<>(List.of(respuesta()), PageRequest.of(0, 20), 1);
        }

        @Override
        public void desactivar(Long id) {
            // Stub sin estado.
        }

        private SedeResponse respuesta() {
            return SedeResponse.builder().id(1L).nombre("Central").activo(true).build();
        }
    }

    private static class EspecialidadServiceStub implements IEspecialidadService {

        @Override
        public EspecialidadResponse crear(EspecialidadCreateRequest solicitud) {
            return respuesta();
        }

        @Override
        public EspecialidadResponse actualizar(Long id, EspecialidadUpdateRequest solicitud) {
            return respuesta();
        }

        @Override
        public EspecialidadResponse obtenerPorId(Long id) {
            return respuesta();
        }

        @Override
        public Page<EspecialidadResponse> listar(Pageable pageable) {
            return new PageImpl<>(List.of(respuesta()), PageRequest.of(0, 20), 1);
        }

        @Override
        public List<EspecialidadResponse> listarTodas() {
            return List.of(respuesta());
        }

        @Override
        public void eliminar(Long id) {
            // Stub sin estado.
        }

        private EspecialidadResponse respuesta() {
            return EspecialidadResponse.builder().id(1L).nombre("Cardiologia").build();
        }
    }

    private static class HistorialServiceStub implements IHistorialService {

        @Override
        public ConsultaResponse crearConsulta(ConsultaCreateRequest solicitud) {
            return respuesta();
        }

        @Override
        public ConsultaResponse obtenerConsulta(Long consultaId) {
            return respuesta();
        }

        @Override
        public Page<ConsultaResponse> listarPorPaciente(Long pacienteId, String search, boolean tieneRecetas, boolean tieneEstudios, boolean tieneAdjuntos, java.time.LocalDate fechaInicio, java.time.LocalDate fechaFin, Pageable pageable) {
            return new PageImpl<>(List.of(respuesta()), PageRequest.of(0, 20), 1);
        }

        @Override
        public Page<ConsultaResponse> listarPorDoctorAutenticado(Pageable pageable) {
            return new PageImpl<>(List.of(respuesta()), PageRequest.of(0, 20), 1);
        }

        @Override
        public ConsultaResponse agregarNotaEvolucion(Long consultaId, NotaEvolucionRequest solicitud) {
            return respuesta();
        }

        @Override
        public AdjuntoResponse agregarAdjunto(Long consultaId, org.springframework.web.multipart.MultipartFile archivo) {
            return AdjuntoResponse.builder()
                    .id(1L)
                    .nombreArchivo(archivo.getOriginalFilename())
                    .tipoMime(archivo.getContentType())
                    .build();
        }

        @Override
        public AdjuntoDownloadResponse descargarAdjunto(Long adjuntoId) {
            return new AdjuntoDownloadResponse(
                    "resultado.pdf",
                    MediaType.APPLICATION_PDF_VALUE,
                    new ByteArrayResource("PDF".getBytes()));
        }

        @Override
        public byte[] generarPdfConsulta(Long consultaId) {
            return "PDF".getBytes();
        }

        @Override
        public Page<com.web.clinica.dto.response.EstudioResponse> listarEstudios(String estado, String filtro, Pageable pageable) {
            com.web.clinica.dto.response.EstudioResponse response = new com.web.clinica.dto.response.EstudioResponse();
            response.setId(1L);
            response.setTipoEstudio("Estudio de prueba");
            response.setEstado("pendiente");
            return new org.springframework.data.domain.PageImpl<>(List.of(response), pageable, 1);
        }

        @Override
        public com.web.clinica.dto.response.EstudioResponse registrarResultadoEstudio(Long estudioId, List<org.springframework.web.multipart.MultipartFile> archivos) {
            com.web.clinica.dto.response.EstudioResponse response = new com.web.clinica.dto.response.EstudioResponse();
            response.setId(estudioId);
            response.setTipoEstudio("Estudio de prueba");
            response.setDetalle("Detalle");
            response.setEstado("realizado");
            return response;
        }

        @Override
        public AdjuntoDownloadResponse descargarResultadoEstudio(Long estudioId, Integer index) {
            return new AdjuntoDownloadResponse("archivo.pdf", "application/pdf", new org.springframework.core.io.ByteArrayResource("archivo".getBytes()));
        }

        private ConsultaResponse respuesta() {
            return ConsultaResponse.builder().id(1L).tipo("consulta").estado("activa").build();
        }
    }


    private static class PagoServiceStub implements IPagoService {

        @Override
        public PagoResponse registrarPago(PagoCreateRequest solicitud) {
            return respuesta();
        }

        @Override
        public PagoResponse obtenerPorCita(Long citaId) {
            return respuesta();
        }

        @Override
        public List<PagoResponse> listarPorPaciente(Long pacienteId) {
            return List.of(respuesta());
        }

        private PagoResponse respuesta() {
            return PagoResponse.builder().id(1L).citaId(1L).monto(BigDecimal.TEN).metodo("efectivo").build();
        }
    }

    private static class DashboardServiceStub implements IDashboardService {

        @Override
        public DashboardTotalesResponse obtenerTotales() {
            return DashboardTotalesResponse.builder()
                    .totalPacientes(1)
                    .totalMedicos(1)
                    .totalCitasProgramadas(1)
                    .citasHoy(1)
                    .build();
        }
    }
}
