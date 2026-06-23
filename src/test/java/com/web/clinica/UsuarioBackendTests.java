package com.web.clinica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.web.clinica.controller.UsuarioController;
import com.web.clinica.dto.request.UsuarioCreateRequest;
import com.web.clinica.dto.request.UsuarioUpdateRequest;
import com.web.clinica.dto.response.UsuarioResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Rol;
import com.web.clinica.model.Secretaria;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.DoctorRepository;
import com.web.clinica.repository.RolRepository;
import com.web.clinica.repository.SecretariaRepository;
import com.web.clinica.repository.UsuarioRepository;
import com.web.clinica.service.abstractService.IUsuarioService;
import com.web.clinica.service.serviceImpl.UsuarioServiceImpl;
import com.web.clinica.util.EmailService;
import java.security.SecureRandom;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class UsuarioBackendTests {

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    private UsuarioRepository usuarioRepository;
    private RolRepository rolRepository;
    private SecretariaRepository secretariaRepository;
    private DoctorRepository doctorRepository;
    private PasswordEncoder passwordEncoder;
    private EmailService emailService;
    private SecureRandom generadorSeguro = new SecureRandom();

    private UsuarioServiceImpl usuarioService;
    private MockMvc mockMvc;

    @BeforeEach
    void setup() {
        usuarioRepository = mock(UsuarioRepository.class);
        rolRepository = mock(RolRepository.class);
        secretariaRepository = mock(SecretariaRepository.class);
        doctorRepository = mock(DoctorRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);

        usuarioService = new UsuarioServiceImpl(
                usuarioRepository,
                rolRepository,
                secretariaRepository,
                doctorRepository,
                passwordEncoder,
                emailService,
                generadorSeguro
        );

        UsuarioController controller = new UsuarioController(usuarioService);
        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();
    }

    @Test
    void testCrearUsuarioExito() {
        UsuarioCreateRequest req = new UsuarioCreateRequest();
        req.setDni("12345678");
        req.setNombres("Juan");
        req.setApellidos("Perez");
        req.setEmail("juan@gmail.com");
        req.setTelefono("999888777");
        req.setFechaNacimiento(LocalDate.of(1990, 1, 1));
        req.setRolesIds(List.of(1L));

        Rol rol = new Rol();
        rol.setId(1L);
        rol.setNombre("Recepcionista");

        when(usuarioRepository.findByDni(any())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(rolRepository.findAllById(any())).thenReturn(List.of(rol));
        when(passwordEncoder.encode(any())).thenReturn("hashed_pass");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> inv.getArgument(0));

        UsuarioResponse resp = usuarioService.crear(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getDni()).isEqualTo("12345678");
        assertThat(resp.getNombres()).isEqualTo("Juan");
        assertThat(resp.getRoles()).hasSize(1);
        assertThat(resp.getRoles().get(0).getNombre()).isEqualTo("Recepcionista");
        verify(emailService, times(1)).enviarCorreo(eq("juan@gmail.com"), anyString(), anyString());
    }

    @Test
    void testCrearUsuarioSecretariaConDoctores() {
        UsuarioCreateRequest req = new UsuarioCreateRequest();
        req.setDni("87654321");
        req.setNombres("Maria");
        req.setApellidos("Gomez");
        req.setEmail("maria@gmail.com");
        req.setRolesIds(List.of(2L));
        req.setDoctorIds(List.of(10L));

        Rol rolSec = new Rol();
        rolSec.setId(2L);
        rolSec.setNombre("Secretaria");

        Doctor doc = new Doctor();
        doc.setId(10L);

        when(usuarioRepository.findByDni(any())).thenReturn(Optional.empty());
        when(usuarioRepository.findByEmail(any())).thenReturn(Optional.empty());
        when(rolRepository.findAllById(any())).thenReturn(List.of(rolSec));
        when(doctorRepository.findAllById(any())).thenReturn(List.of(doc));
        when(passwordEncoder.encode(any())).thenReturn("hashed_pass");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
            Usuario u = inv.getArgument(0);
            u.setId(100L);
            return u;
        });

        Secretaria sec = new Secretaria();
        sec.setId(5L);
        sec.setUsuario(new Usuario());
        sec.setDoctores(Set.of(doc));

        when(secretariaRepository.save(any(Secretaria.class))).thenReturn(sec);
        when(secretariaRepository.findByUsuarioId(100L)).thenReturn(Optional.of(sec));

        UsuarioResponse resp = usuarioService.crear(req);

        assertThat(resp).isNotNull();
        assertThat(resp.getDoctorIds()).containsExactly(10L);
        verify(secretariaRepository, times(1)).save(any(Secretaria.class));
    }

    @Test
    void testDesactivarUsuario() {
        Usuario usuario = new Usuario();
        usuario.setId(1L);
        usuario.setActivo(true);

        when(usuarioRepository.findById(1L)).thenReturn(Optional.of(usuario));
        when(usuarioRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        usuarioService.desactivar(1L);

        assertThat(usuario.getActivo()).isFalse();
    }

    @Test
    void testListarUsuariosEndpoint() throws Exception {
        UsuarioResponse userResp = UsuarioResponse.builder()
                .id(1L)
                .dni("12345678")
                .nombres("Juan")
                .apellidos("Perez")
                .activo(true)
                .roles(List.of())
                .build();

        IUsuarioService mockService = mock(IUsuarioService.class);
        when(mockService.listar(any(Pageable.class))).thenReturn(new PageImpl<>(List.of(userResp), PageRequest.of(0, 10), 1));

        UsuarioController controller = new UsuarioController(mockService);
        MockMvc localMockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver())
                .build();

        localMockMvc.perform(get("/api/usuarios")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].dni").value("12345678"))
                .andExpect(jsonPath("$.content[0].nombres").value("Juan"));
    }
}
