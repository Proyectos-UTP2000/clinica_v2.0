package com.web.clinica;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import com.web.clinica.service.abstractService.ICajaDiariaService;

@SpringBootTest
class ClinicaApplicationTests {

    @Autowired
    private ICajaDiariaService cajaDiariaService;

    @Test
    void contextoCargaCorrectamente() {
    }

    @Test
    void testReabrirCajaReal() {
        try {
            cajaDiariaService.reabrirCaja();
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }
}
