package com.rhu.api_platform.planilla.motor;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

/**
 * Resumen del impacto de ausencias/incapacidades en un período de planilla.
 */
@Data
@Builder
public class ResumenAusencias {

    /** Días a descontar completamente del salario proporcional. */
    @Builder.Default
    private int diasDescontar = 0;

    /** Días a pagar con porcentaje reducido (incapacidad común días 1-3). */
    @Builder.Default
    private int diasPagoParcial = 0;

    /** Porcentaje a aplicar a los días de pago parcial. */
    @Builder.Default
    private BigDecimal porcentajePagoParcial = BigDecimal.ZERO;

    /** Horas a descontar del salario proporcional (ausencias por horas). */
    @Builder.Default
    private BigDecimal horasDescontar = BigDecimal.ZERO;

    /** Días a reportar a ISSS/AFP sin pago en planilla (incapacidad ISSS total). */
    @Builder.Default
    private int diasReportarIsssAfp = 0;

    /** Semanas en las que ocurrió al menos una falta injustificada. */
    @Builder.Default
    private int semanasConFaltaInjustificada = 0;

    public boolean tieneAusencias() {
        return diasDescontar > 0
                || diasPagoParcial > 0
                || horasDescontar.compareTo(BigDecimal.ZERO) > 0
                || diasReportarIsssAfp > 0
                || semanasConFaltaInjustificada > 0;
    }
}
