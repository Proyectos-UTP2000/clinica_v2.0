package com.web.clinica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.web.clinica.dto.response.AdjuntoDownloadResponse;
import com.web.clinica.dto.response.AdjuntoResponse;
import com.web.clinica.model.Adjunto;
import com.web.clinica.model.Consulta;
import com.web.clinica.model.Doctor;
import com.web.clinica.model.Paciente;
import com.web.clinica.model.Sede;
import com.web.clinica.model.Usuario;
import com.web.clinica.repository.AdjuntoRepository;
import com.web.clinica.repository.CitaRepository;
import com.web.clinica.repository.ConsultaRepository;
import com.web.clinica.repository.DoctorRepository;
import com.web.clinica.repository.EstudioComplementarioRepository;
import com.web.clinica.repository.IndicacionMedicaRepository;
import com.web.clinica.repository.NotaEvolucionRepository;
import com.web.clinica.repository.PacienteRepository;
import com.web.clinica.repository.RecetaRepository;
import com.web.clinica.repository.SedeRepository;
import com.web.clinica.service.serviceImpl.HistorialServiceImpl;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

class HistorialAdjuntoServiceTests {

    @TempDir
    Path tempDir;

    @Test
    void guardaArchivoAdjuntoYPermiteDescargarlo() throws Exception {
        ConsultaRepository consultaRepository = mock(ConsultaRepository.class);
        AdjuntoRepository adjuntoRepository = mock(AdjuntoRepository.class);
        Consulta consulta = consulta();
        AtomicReference<Adjunto> adjuntoGuardado = new AtomicReference<>();
        when(consultaRepository.findById(1L)).thenReturn(Optional.of(consulta));
        when(adjuntoRepository.save(any(Adjunto.class))).thenAnswer(invocation -> {
            Adjunto adjunto = invocation.getArgument(0);
            adjunto.setId(7L);
            adjuntoGuardado.set(adjunto);
            return adjunto;
        });
        HistorialServiceImpl service = service(consultaRepository, adjuntoRepository);
        SecurityContextHolder.getContext().setAuthentication(new TestingAuthenticationToken(
                consulta.getDoctor().getUsuario(), null, "historial.ver_todos"));

        MockMultipartFile archivo = new MockMultipartFile(
                "archivo",
                "resultado.pdf",
                "application/pdf",
                "contenido".getBytes());
        AdjuntoResponse response = service.agregarAdjunto(1L, archivo);

        assertThat(response.getId()).isEqualTo(7L);
        assertThat(response.getNombreArchivo()).isEqualTo("resultado.pdf");
        assertThat(Files.exists(tempDir.resolve(response.getRuta()))).isTrue();

        when(adjuntoRepository.findById(7L)).thenReturn(Optional.of(adjuntoGuardado.get()));

        AdjuntoDownloadResponse descarga = service.descargarAdjunto(7L);

        assertThat(descarga.nombreArchivo()).isEqualTo("resultado.pdf");
        assertThat(descarga.tipoMime()).isEqualTo("application/pdf");
        assertThat(descarga.resource().getContentAsByteArray()).isEqualTo("contenido".getBytes());
    }

    private HistorialServiceImpl service(ConsultaRepository consultaRepository, AdjuntoRepository adjuntoRepository) {
        return new HistorialServiceImpl(
                consultaRepository,
                mock(RecetaRepository.class),
                mock(IndicacionMedicaRepository.class),
                mock(EstudioComplementarioRepository.class),
                adjuntoRepository,
                mock(NotaEvolucionRepository.class),
                mock(PacienteRepository.class),
                mock(DoctorRepository.class),
                mock(SedeRepository.class),
                mock(CitaRepository.class),
                tempDir,
                mock(com.cloudinary.Cloudinary.class));
    }


    private Consulta consulta() {
        Usuario usuario = new Usuario();
        usuario.setId(5L);
        usuario.setNombres("Ada");
        usuario.setApellidos("Lovelace");
        Doctor doctor = new Doctor();
        doctor.setId(3L);
        doctor.setUsuario(usuario);
        Paciente paciente = new Paciente();
        paciente.setId(2L);
        paciente.setNombres("Ana");
        paciente.setApellidos("Rojas");
        Sede sede = new Sede();
        sede.setId(4L);
        sede.setNombre("Central");
        Consulta consulta = new Consulta();
        consulta.setId(1L);
        consulta.setDoctor(doctor);
        consulta.setPaciente(paciente);
        consulta.setSede(sede);
        return consulta;
    }
}
