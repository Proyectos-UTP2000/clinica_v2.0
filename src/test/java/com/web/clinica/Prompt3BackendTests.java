package com.web.clinica;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.web.clinica.controller.MedicoController;
import com.web.clinica.controller.PacienteController;
import com.web.clinica.dto.request.MedicoCreateRequest;
import com.web.clinica.dto.request.MedicoUpdateRequest;
import com.web.clinica.dto.request.PacienteCreateRequest;
import com.web.clinica.dto.request.PacienteUpdateRequest;
import com.web.clinica.dto.response.MedicoResponse;
import com.web.clinica.dto.response.PacienteResponse;
import com.web.clinica.service.abstractService.IMedicoService;
import com.web.clinica.service.abstractService.IPacienteService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class Prompt3BackendTests {

    @Test
    void endpointsPacientesBuscarDniRespondenContratoEsperado() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new PacienteController(new PacienteServiceStub()))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        mockMvc.perform(get("/api/pacientes/buscar").param("dni", "70135060"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("70135060"))
                .andExpect(jsonPath("$.nombres").value("YOVANA LISBETH"));
        mockMvc.perform(get("/api/pacientes/buscar-dni").param("dni", "70135060"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dni").value("70135060"))
                .andExpect(jsonPath("$.apellidos").value("MAMANI FAIJO"));
    }

    @Test
    void endpointsMedicosBuscarRespondenContratoEsperado() throws Exception {
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new MedicoController(new MedicoServiceStub()))
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        mockMvc.perform(get("/api/medicos/buscar")
                        .param("sedeId", "1")
                        .param("especialidadId", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dni").value("70135060"));
        mockMvc.perform(get("/api/medicos/buscar-dni").param("dni", "70135060"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nombres").value("YOVANA LISBETH"))
                .andExpect(jsonPath("$.apellidos").value("MAMANI FAIJO"));
    }

    private static class PacienteServiceStub implements IPacienteService {

        @Override
        public PacienteResponse crear(PacienteCreateRequest solicitud) {
            return pacienteExistente();
        }

        @Override
        public PacienteResponse actualizar(Long id, PacienteUpdateRequest solicitud) {
            return pacienteExistente();
        }

        @Override
        public PacienteResponse obtenerPorId(Long id) {
            return pacienteExistente();
        }

        @Override
        public PacienteResponse buscarPorDni(String dni) {
            return pacienteExistente();
        }

        @Override
        public PacienteResponse consultarDni(String dni) {
            return personaDni();
        }

        @Override
        public Page<PacienteResponse> listarActivos(Pageable pageable) {
            return new PageImpl<>(List.of(pacienteExistente()), PageRequest.of(0, 20), 1);
        }

        @Override
        public void desactivar(Long id) {
            // Stub sin estado.
        }

        private PacienteResponse pacienteExistente() {
            return PacienteResponse.builder()
                    .id(1L)
                    .dni("70135060")
                    .nombres("YOVANA LISBETH")
                    .apellidos("MAMANI FAIJO")
                    .activo(true)
                    .build();
        }

        private PacienteResponse personaDni() {
            return PacienteResponse.builder()
                    .dni("70135060")
                    .nombres("YOVANA LISBETH")
                    .apellidos("MAMANI FAIJO")
                    .build();
        }
    }

    private static class MedicoServiceStub implements IMedicoService {

        @Override
        public MedicoResponse crear(MedicoCreateRequest solicitud) {
            return medico();
        }

        @Override
        public MedicoResponse actualizar(Long id, MedicoUpdateRequest solicitud) {
            return medico();
        }

        @Override
        public MedicoResponse obtenerPorId(Long id) {
            return medico();
        }

        @Override
        public MedicoResponse obtenerAutenticado() {
            return medico();
        }

        @Override
        public Page<MedicoResponse> listarActivos(String texto, Long especialidadId, Long sedeId, Pageable pageable) {
            return new PageImpl<>(List.of(medico()), PageRequest.of(0, 20), 1);
        }

        @Override
        public MedicoResponse consultarDni(String dni) {
            return MedicoResponse.builder()
                    .dni("70135060")
                    .nombres("YOVANA LISBETH")
                    .apellidos("MAMANI FAIJO")
                    .build();
        }

        @Override
        public void desactivar(Long id) {
            // Stub sin estado.
        }

        private MedicoResponse medico() {
            return MedicoResponse.builder()
                    .id(1L)
                    .dni("70135060")
                    .nombres("YOVANA LISBETH")
                    .apellidos("MAMANI FAIJO")
                    .build();
        }
    }
}
