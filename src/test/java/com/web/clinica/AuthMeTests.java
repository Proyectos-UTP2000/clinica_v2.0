package com.web.clinica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.web.clinica.controller.AuthController;
import com.web.clinica.dto.response.JwtResponse;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.CodigoVerificacionRepository;
import com.web.clinica.repository.UsuarioRepository;
import com.web.clinica.service.abstractService.IAuthService;
import com.web.clinica.service.serviceImpl.AuthServiceImpl;
import com.web.clinica.util.EmailService;
import java.security.SecureRandom;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class AuthMeTests {

    @Test
    void endpointMeDevuelvePermisosActualizadosDelUsuarioAutenticado() throws Exception {
        IAuthService authService = mock(IAuthService.class);
        Usuario usuario = new Usuario();
        usuario.setId(9L);
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(new AuthController(authService)).build();

        when(authService.obtenerSesionActual(9L)).thenReturn(JwtResponse.builder()
                .token("jwt-refrescado")
                .dni("12345678")
                .nombres("Ada")
                .apellidos("Lovelace")
                .roles(List.of("Administrador"))
                .permisos(List.of("dashboard.ver", "pacientes.ver"))
                .build());

        mockMvc.perform(get("/api/auth/me")
                        .principal(new UsernamePasswordAuthenticationToken(usuario, null)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("jwt-refrescado"))
                .andExpect(jsonPath("$.permisos[0]").value("dashboard.ver"))
                .andExpect(jsonPath("$.permisos[1]").value("pacientes.ver"));
    }

    @Test
    void obtenerSesionActualReconstruyePermisosDesdeBaseDeDatos() {
        UsuarioRepository usuarioRepository = mock(UsuarioRepository.class);
        com.web.clinica.config.JwtProvider jwtProvider = mock(com.web.clinica.config.JwtProvider.class);
        AuthServiceImpl service = new AuthServiceImpl(
                usuarioRepository,
                mock(CodigoVerificacionRepository.class),
                null,
                jwtProvider,
                mock(EmailService.class),
                new SecureRandom()
        );
        Usuario usuario = TestAuthData.usuarioConRolPermisos("12345678", "Administrador", "dashboard.ver");
        usuario.setId(4L);

        when(usuarioRepository.findById(4L)).thenReturn(Optional.of(usuario));
        when(jwtProvider.generarToken(usuario)).thenReturn("jwt-nuevo");

        JwtResponse response = service.obtenerSesionActual(4L);

        assertThat(response.getToken()).isEqualTo("jwt-nuevo");
        assertThat(response.getPermisos()).containsExactly("dashboard.ver");
    }
}
