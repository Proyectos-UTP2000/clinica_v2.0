package com.web.clinica.util;

public interface DniApiClient {

    /** Consulta datos civiles basicos asociados a un DNI. */
    DniInfo consultarDni(String dni);
}
