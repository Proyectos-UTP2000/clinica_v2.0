package com.web.clinica;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.web.clinica.controller.CitaController;
import com.web.clinica.dto.request.CitaCreateRequest;
import com.web.clinica.dto.request.CitaUpdateRequest;
import com.web.clinica.service.abstractService.ICitaService;
import java.time.LocalDateTime;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class CitaValidationTests {
    private MockMvc mockMvc;
    private ICitaService citaService;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setup() {
        citaService = Mockito.mock(ICitaService.class);
        mockMvc = MockMvcBuilders.standaloneSetup(new CitaController(citaService)).build();
        objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());
    }

    @Test
    void crearCitaConFechaPasadaFalla() throws Exception {
        CitaCreateRequest req = new CitaCreateRequest();
        req.setPacienteId(1L);
        req.setDoctorId(1L);
        req.setSedeId(1L);
        req.setConsultorioId(1L);
        req.setFechaHoraInicio(LocalDateTime.now().minusDays(1)); // fecha pasada
        req.setPagoAnticipado(false);

        mockMvc.perform(post("/api/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void reprogramarCitaConFechaPasadaFalla() throws Exception {
        CitaUpdateRequest req = new CitaUpdateRequest();
        req.setNuevaFechaHora(LocalDateTime.now().minusDays(1)); // fecha pasada
        req.setDoctorId(1L);

        mockMvc.perform(put("/api/citas/1/reprogramar")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isBadRequest());
    }
}
