package com.web.clinica.dto.response;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class DisponibilidadSlotResponse {

    private LocalDateTime inicio;
    private LocalDateTime fin;
}
