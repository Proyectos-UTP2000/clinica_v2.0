package com.web.clinica.dto.response;

import java.time.LocalDate;
import java.util.List;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MedicoResponse {

    private Long id;
    private Long usuarioId;
    private String dni;
    private String nombres;
    private String apellidos;
    private String email;
    private String telefono;
    private LocalDate fechaNacimiento;
    private String especialidadNombre;
    private String subespecialidadNombre;
    private List<String> sedes;
    private Boolean activo;
}
