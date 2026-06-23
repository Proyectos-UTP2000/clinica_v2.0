package com.web.clinica.util;

public class DniApiClientMock implements DniApiClient {

    /** Devuelve informacion mock hasta integrar la API externa real. */
    @Override
    public DniInfo consultarDni(String dni) {
        return new DniInfo(dni, "Nombres Mock", "Apellidos Mock");
    }
}
