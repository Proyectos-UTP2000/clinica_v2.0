package com.web.clinica.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "caja_diaria")
public class CajaDiaria {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private LocalDate fecha;

    @Column(name = "monto_apertura", nullable = false, precision = 10, scale = 2)
    private BigDecimal montoApertura;

    @Column(name = "monto_cierre", precision = 10, scale = 2)
    private BigDecimal montoCierre;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal ingresos = BigDecimal.ZERO;

    @Builder.Default
    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal egresos = BigDecimal.ZERO;

    @Column(name = "balance_real", precision = 10, scale = 2)
    private BigDecimal balanceReal;

    @Column(precision = 10, scale = 2)
    private BigDecimal diferencia;

    @Column(nullable = false, length = 20)
    private String estado; // "abierta", "cerrada"

    @Builder.Default
    @Column(name = "fecha_apertura", nullable = false)
    private LocalDateTime fechaApertura = LocalDateTime.now();

    @Column(name = "fecha_cierre")
    private LocalDateTime fechaCierre;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "abierto_por_usuario_id")
    private Usuario abiertoPorUsuario;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cerrado_por_usuario_id")
    private Usuario cerradoPorUsuario;

    @Column(columnDefinition = "TEXT")
    private String observaciones;
}
