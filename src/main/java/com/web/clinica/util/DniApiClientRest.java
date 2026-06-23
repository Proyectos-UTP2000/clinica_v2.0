package com.web.clinica.util;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.web.clinica.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class DniApiClientRest implements DniApiClient {

    private final RestTemplate restTemplate;
    private final String urlBase;
    private final String token;

    public DniApiClientRest(RestTemplateBuilder restTemplateBuilder,
                            @Value("${app.dni-api.url}") String urlBase,
                            @Value("${app.dni-api.token}") String token) {
        this(restTemplateBuilder.build(), urlBase, token);
    }

    public DniApiClientRest(RestTemplate restTemplate, String urlBase, String token) {
        this.restTemplate = restTemplate;
        this.urlBase = urlBase.endsWith("/") ? urlBase : urlBase + "/";
        this.token = token;
    }

    /** Consulta la API externa de DNI y normaliza nombres/apellidos para formularios. */
    @Override
    public DniInfo consultarDni(String dni) {
        if (dni == null || !dni.matches("\\d{8}")) {
            throw new BadRequestException("El DNI debe tener 8 digitos");
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(token);
        ResponseEntity<DniApiResponse> response = restTemplate.exchange(
                urlBase + dni,
                HttpMethod.GET,
                new HttpEntity<>(headers),
                DniApiResponse.class
        );

        DniApiResponse body = response.getBody();
        if (body == null || !body.success() || body.datos() == null) {
            throw new BadRequestException("No se encontraron datos para el DNI indicado");
        }
        DniDatos datos = body.datos();
        String apellidos = String.join(" ", datos.apePaterno(), datos.apeMaterno()).trim();
        return new DniInfo(datos.dni(), datos.nombres(), apellidos);
    }

    private record DniApiResponse(boolean success, DniDatos datos) {
    }

    private record DniDatos(
            String dni,
            String nombres,
            @JsonProperty("ape_paterno") String apePaterno,
            @JsonProperty("ape_materno") String apeMaterno
    ) {
    }
}
