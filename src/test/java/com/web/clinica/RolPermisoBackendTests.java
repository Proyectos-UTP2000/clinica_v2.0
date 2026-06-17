package com.web.clinica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.web.clinica.controller.PermisoController;
import com.web.clinica.controller.RolController;
import com.web.clinica.dto.request.AsignarPermisosRequest;
import com.web.clinica.dto.request.RolCreateRequest;
import com.web.clinica.dto.request.RolUpdateRequest;
import com.web.clinica.dto.response.PermisoResponse;
import com.web.clinica.dto.response.RolResponse;
import com.web.clinica.exception.BadRequestException;
import com.web.clinica.exception.ResourceNotFoundException;
import com.web.clinica.model.Permiso;
import com.web.clinica.model.Rol;
import com.web.clinica.repository.PermisoRepository;
import com.web.clinica.repository.RolRepository;
import com.web.clinica.service.abstractService.IPermisoService;
import com.web.clinica.service.abstractService.IRolService;
import com.web.clinica.service.serviceImpl.PermisoServiceImpl;
import com.web.clinica.service.serviceImpl.RolServiceImpl;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

class RolPermisoBackendTests {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void endpointsRolesYPermisosRespondenConContratoEsperado() throws Exception {
        IRolService rolService = new RolServiceStub();
        IPermisoService permisoService = () -> List.of(PermisoResponse.builder()
                .id(1L).codigo("roles.ver").descripcion("Ver lista de roles").build());
        MockMvc mockMvc = MockMvcBuilders.standaloneSetup(
                new RolController(rolService),
                new PermisoController(permisoService)
        ).setCustomArgumentResolvers(new PageableHandlerMethodArgumentResolver()).build();

        mockMvc.perform(get("/api/roles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].nombre").value("Administrador"));
        mockMvc.perform(get("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos[0].codigo").value("roles.ver"));
        mockMvc.perform(post("/api/roles").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rolCrear())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.activo").value(true));
        mockMvc.perform(put("/api/roles/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(rolActualizar())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.descripcion").value("Gestion completa"));
        mockMvc.perform(put("/api/roles/1/permisos").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(asignarPermisos())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.permisos.length()").value(1));
        mockMvc.perform(delete("/api/roles/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
        mockMvc.perform(get("/api/permisos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].codigo").value("roles.ver"));
    }

    @Test
    void crearRolAsignaPermisosYEvitaNombreDuplicado() {
        RolRepository rolRepository = mock(RolRepository.class);
        PermisoRepository permisoRepository = mock(PermisoRepository.class);
        RolServiceImpl service = new RolServiceImpl(rolRepository, permisoRepository);
        RolCreateRequest solicitud = rolCrear();
        Permiso permiso = permiso(1L, "roles.ver");

        when(rolRepository.findByNombre("Administrador")).thenReturn(Optional.empty());
        when(permisoRepository.findAllById(List.of(1L))).thenReturn(List.of(permiso));
        when(rolRepository.save(any(Rol.class))).thenAnswer(invocation -> {
            Rol rol = invocation.getArgument(0);
            rol.setId(10L);
            return rol;
        });

        RolResponse respuesta = service.crear(solicitud);

        assertThat(respuesta.getId()).isEqualTo(10L);
        assertThat(respuesta.getNombre()).isEqualTo("Administrador");
        assertThat(respuesta.getActivo()).isTrue();
        assertThat(respuesta.getPermisos()).extracting(PermisoResponse::getCodigo).containsExactly("roles.ver");

        when(rolRepository.findByNombre("Administrador")).thenReturn(Optional.of(new Rol()));
        assertThatThrownBy(() -> service.crear(solicitud))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Ya existe un rol");
    }

    @Test
    void actualizarPermisosReemplazaChecklistCompletoYValidaIds() {
        RolRepository rolRepository = mock(RolRepository.class);
        PermisoRepository permisoRepository = mock(PermisoRepository.class);
        RolServiceImpl service = new RolServiceImpl(rolRepository, permisoRepository);
        Rol rol = rol(5L, "Secretaria", true);
        Permiso ver = permiso(1L, "roles.ver");
        Permiso editar = permiso(2L, "roles.editar");
        rol.getPermisos().add(ver);

        when(rolRepository.findById(5L)).thenReturn(Optional.of(rol));
        when(permisoRepository.findAllById(List.of(2L))).thenReturn(List.of(editar));
        when(rolRepository.save(any(Rol.class))).thenAnswer(invocation -> invocation.getArgument(0));

        AsignarPermisosRequest solicitud = new AsignarPermisosRequest();
        solicitud.setPermisosIds(List.of(2L));
        RolResponse respuesta = service.asignarPermisos(5L, solicitud);

        assertThat(respuesta.getPermisos()).extracting(PermisoResponse::getCodigo).containsExactly("roles.editar");
        assertThat(rol.getPermisos()).extracting(Permiso::getCodigo).containsExactly("roles.editar");

        solicitud.setPermisosIds(List.of(1L, 999L));
        when(permisoRepository.findAllById(List.of(1L, 999L))).thenReturn(List.of(ver));
        assertThatThrownBy(() -> service.asignarPermisos(5L, solicitud))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Permisos no encontrados");
    }

    @Test
    void desactivarRolMarcaInactivoSinEliminarFisicamente() {
        RolRepository rolRepository = mock(RolRepository.class);
        PermisoRepository permisoRepository = mock(PermisoRepository.class);
        RolServiceImpl service = new RolServiceImpl(rolRepository, permisoRepository);
        Rol rol = rol(3L, "Auditor", true);

        when(rolRepository.findById(3L)).thenReturn(Optional.of(rol));

        service.desactivar(3L);

        assertThat(rol.getActivo()).isFalse();
        verify(rolRepository).save(rol);
        verify(rolRepository, never()).delete(any());
    }

    @Test
    void listarPermisosDevuelveCodigosOrdenados() {
        PermisoRepository permisoRepository = mock(PermisoRepository.class);
        PermisoServiceImpl service = new PermisoServiceImpl(permisoRepository);

        when(permisoRepository.findAll()).thenReturn(List.of(
                permiso(2L, "usuarios.ver"),
                permiso(1L, "roles.ver")
        ));

        List<PermisoResponse> respuesta = service.listar();

        assertThat(respuesta).extracting(PermisoResponse::getCodigo)
                .containsExactly("roles.ver", "usuarios.ver");
    }

    @Test
    void obtenerRolInexistenteLanzaNoEncontrado() {
        RolRepository rolRepository = mock(RolRepository.class);
        PermisoRepository permisoRepository = mock(PermisoRepository.class);
        RolServiceImpl service = new RolServiceImpl(rolRepository, permisoRepository);

        when(rolRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.obtenerPorId(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Rol no encontrado");
    }

    private RolCreateRequest rolCrear() {
        RolCreateRequest solicitud = new RolCreateRequest();
        solicitud.setNombre("Administrador");
        solicitud.setDescripcion("Gestion total");
        solicitud.setPermisosIds(List.of(1L));
        return solicitud;
    }

    private RolUpdateRequest rolActualizar() {
        RolUpdateRequest solicitud = new RolUpdateRequest();
        solicitud.setNombre("Administrador");
        solicitud.setDescripcion("Gestion completa");
        solicitud.setActivo(true);
        solicitud.setPermisosIds(List.of(1L));
        return solicitud;
    }

    private AsignarPermisosRequest asignarPermisos() {
        AsignarPermisosRequest solicitud = new AsignarPermisosRequest();
        solicitud.setPermisosIds(List.of(1L));
        return solicitud;
    }

    private Permiso permiso(Long id, String codigo) {
        Permiso permiso = new Permiso();
        permiso.setId(id);
        permiso.setCodigo(codigo);
        permiso.setDescripcion("Descripcion " + codigo);
        return permiso;
    }

    private Rol rol(Long id, String nombre, Boolean activo) {
        Rol rol = new Rol();
        rol.setId(id);
        rol.setNombre(nombre);
        rol.setDescripcion("Descripcion " + nombre);
        rol.setActivo(activo);
        return rol;
    }

    private static class RolServiceStub implements IRolService {

        @Override
        public Page<RolResponse> listar(Pageable pageable) {
            return new PageImpl<>(List.of(respuesta()), PageRequest.of(0, 20), 1);
        }

        @Override
        public RolResponse obtenerPorId(Long id) {
            return respuesta();
        }

        @Override
        public RolResponse crear(RolCreateRequest solicitud) {
            return respuesta();
        }

        @Override
        public RolResponse actualizar(Long id, RolUpdateRequest solicitud) {
            return RolResponse.builder()
                    .id(id)
                    .nombre(solicitud.getNombre())
                    .descripcion(solicitud.getDescripcion())
                    .activo(true)
                    .permisos(List.of(permisoRespuesta()))
                    .build();
        }

        @Override
        public RolResponse asignarPermisos(Long id, AsignarPermisosRequest solicitud) {
            return respuesta();
        }

        @Override
        public void desactivar(Long id) {
            // Stub sin estado.
        }

        private RolResponse respuesta() {
            return RolResponse.builder()
                    .id(1L)
                    .nombre("Administrador")
                    .descripcion("Gestion total")
                    .activo(true)
                    .permisos(List.of(permisoRespuesta()))
                    .build();
        }

        private PermisoResponse permisoRespuesta() {
            return PermisoResponse.builder()
                    .id(1L)
                    .codigo("roles.ver")
                    .descripcion("Ver lista de roles")
                    .build();
        }
    }
}
