package com.rhu.api_platform.planilla.motor;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Parámetros legales cargados de la tabla PARAMETRO_LEGAL
 * para ejecutar un cálculo de planilla.
 */
@Data
@Builder
public class ParametrosCalculo {
    // ISSS trabajador
    private BigDecimal isssPorc;          // 0.03
    private BigDecimal isssBasMaxima;      // 1000.00
    // ISSS patrono
    private BigDecimal aportePatronalIsssPorc;  // 0.075
    // AFP trabajador
    private BigDecimal afpPorc;           // 0.0725
    // AFP patrono
    private BigDecimal aportePatronalAfpPorc;   // 0.0875
    // ISR tramos
    private BigDecimal isrExento;         // 550.00
    private BigDecimal isrTramo2Inicio;   // 550.01
    private BigDecimal isrTramo2Fin;      // 895.24
    private BigDecimal isrTramo2Porc;     // 0.10
    private BigDecimal isrTramo2Cuota;    // 17.67
    private BigDecimal isrTramo3Inicio;   // 895.25
    private BigDecimal isrTramo3Fin;      // 2038.10
    private BigDecimal isrTramo3Porc;     // 0.20
    private BigDecimal isrTramo3Cuota;    // 60.00
    private BigDecimal isrTramo4Inicio;   // 2038.11
    private BigDecimal isrTramo4Porc;     // 0.30
    private BigDecimal isrTramo4Cuota;    // 288.57
    // Horas laborales mensuales estándar
    private BigDecimal horasMensuales;    // 240 (30 días × 8 horas)

    // Quincena 25
    private Boolean quincena25Activa;      // true/false
    private BigDecimal quincena25TopeSalario; // 1500.00
}
