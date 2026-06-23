package com.web.clinica;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.header;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.web.clinica.exception.BadRequestException;
import com.web.clinica.util.DniApiClientRest;
import com.web.clinica.util.DniInfo;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

class DniApiClientRestTests {

    @Test
    void consultarDniMapeaRespuestaRealDeApiPeru() {
        RestTemplate restTemplate = new RestTemplate();
        MockRestServiceServer server = MockRestServiceServer.bindTo(restTemplate).build();
        DniApiClientRest client = new DniApiClientRest(restTemplate, "https://miapi.cloud/v1/dni/", "token-test");
        server.expect(requestTo("https://miapi.cloud/v1/dni/70135060"))
                .andExpect(method(HttpMethod.GET))
                .andExpect(header(HttpHeaders.AUTHORIZATION, "Bearer token-test"))
                .andRespond(withSuccess("""
                        {
                          "success": true,
                          "datos": {
                            "dni": "70135060",
                            "nombres": "YOVANA LISBETH",
                            "ape_paterno": "MAMANI",
                            "ape_materno": "FAIJO",
                            "domiciliado": {
                              "direccion": "COMUNID. SANTA ROSA DE UYUNI",
                              "distrito": "QUILCAPUNCU",
                              "provincia": "SAN ANTONIO DE PUTINA",
                              "departamento": "PUNO",
                              "ubigeo": "211004"
                            }
                          }
                        }
                        """, MediaType.APPLICATION_JSON));

        DniInfo respuesta = client.consultarDni("70135060");

        assertThat(respuesta.dni()).isEqualTo("70135060");
        assertThat(respuesta.nombres()).isEqualTo("YOVANA LISBETH");
        assertThat(respuesta.apellidos()).isEqualTo("MAMANI FAIJO");
        server.verify();
    }

    @Test
    void consultarDniRechazaDniSinOchoDigitos() {
        RestTemplate restTemplate = new RestTemplate();
        DniApiClientRest client = new DniApiClientRest(restTemplate, "https://miapi.cloud/v1/dni/", "token-test");

        assertThatThrownBy(() -> client.consultarDni("123"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("DNI debe tener 8 digitos");
    }
}
