package com.web.clinica.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "configuracion_global")
public class ConfiguracionGlobal {

    @Id
    @Column(length = 50)
    private String clave;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String valor;
}
