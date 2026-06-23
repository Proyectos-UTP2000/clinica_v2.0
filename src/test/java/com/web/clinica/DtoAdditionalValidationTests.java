package com.web.clinica;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.web.clinica.controller.PacienteController;
import com.web.clinica.controller.PagoController;
import com.web.clinica.dto.request.PacienteCreateRequest;
import com.web.clinica.dto.request.PagoCreateRequest;
import com.web.clinica.service.abstractService.IPacienteService;
import com.web.clinica.service.abstractService.IPagoService;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class DtoAdditionalValidationTests {
    private MockMvc pacienteMockMvc;
    private MockMvc pagoMockMvc;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        IPacienteService pacienteService = Mockito.mock(IPacienteService.class);
        IPagoService pagoService = Mockito.mock(IPagoService.class);
        pacienteMockMvc = MockMvcBuilders.standaloneSetup(new PacienteController(pacienteService)).build();
        pagoMockMvc = MockMvcBuilders.standaloneSetup(new PagoController(pagoService)).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void crearPacienteConDniLargoFalla() throws Exception {
        PacienteCreateRequest req = new PacienteCreateRequest();
        req.setDni("123456789"); // 9 digitos (debe ser max 8)
        req.setNombres("Juan");
        req.setApellidos("Perez");
        req.setFechaNacimiento(LocalDate.now().minusYears(20));
        req.setTelefono("987654321");

        pacienteMockMvc.perform(post("/api/pacientes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void registrarPagoConMontoCeroFalla() throws Exception {
        PagoCreateRequest req = new PagoCreateRequest();
        req.setCitaId(1L);
        req.setMonto(new BigDecimal("0.00")); // monto cero (debe ser min 0.01)
        req.setMetodo("tarjeta");

        pagoMockMvc.perform(post("/api/pagos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
