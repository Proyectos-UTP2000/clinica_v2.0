package com.web.clinica;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.web.clinica.service.abstractService.IHistorialService;
import com.web.clinica.service.abstractService.ICitaService;

@SpringBootTest
class ClinicaApplicationTests {

    @Autowired
    private IHistorialService historialService;

    @Autowired
    private ICitaService citaService;

    @Test
    void contextoCargaCorrectamente() {
    }

    @Test
    void testListarPorPacienteReal() {
        try {
            var resultado = historialService.listarPorPaciente(1L, null, false, false, false, null, null, org.springframework.data.domain.PageRequest.of(0, 500));
            System.out.println("RESULTADO HISTORIAL: " + resultado.getContent().size());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }

    @Test
    void testCheckInReal() {
        try {
            var resultado = citaService.checkIn(9L);
            System.out.println("RESULTADO CHECKIN: " + resultado.getEstado());
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
