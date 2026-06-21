package com.rhu.api_platform.planilla.motor;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

/**
 * Resultado del cálculo de nómina para un empleado en un período.
 */
@Data
@Builder
public class ResultadoCalculo {
    private BigDecimal salarioProporcional;
    private BigDecimal montoHorasExtraDiurnas;
    private BigDecimal montoHorasExtraNocturnas;
    private BigDecimal comisiones;
    private BigDecimal bonificaciones;
    private BigDecimal salarioBruto;

    private BigDecimal isss;
    private BigDecimal afp;
    private BigDecimal baseGravada;
    private BigDecimal isr;

    private BigDecimal descuentosVoluntarios;
    private BigDecimal salarioNeto;

    private BigDecimal aportePatronalIsss;
    private BigDecimal aportePatronalAfp;
}
