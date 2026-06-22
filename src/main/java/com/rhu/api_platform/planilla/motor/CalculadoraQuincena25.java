package com.rhu.api_platform.planilla.motor;

import com.rhu.api_platform.empleado.entity.Empleado;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/**
 * Calcula el beneficio de Quincena 25 (renta no gravable).
 *
 * Reglas aplicadas:
 * - Solo aplica si el parámetro QUINCENA_25_ACTIVA es true.
 * - Elegibles: empleados activos con salario base <= QUINCENA_25_TOPE_SALARIO.
 * - Beneficio base = 50% del salario base mensual.
 * - Antigüedad >= 1 año al 25 de enero: 100% del beneficio.
 * - Antigüedad < 1 año: proporcional por días trabajados / 365.
 * - No aplica ISR, ISSS, AFP ni descuentos voluntarios.
 */
@Component
public class CalculadoraQuincena25 {

    private static final BigDecimal PORCENTAJE_BENEFICIO = new BigDecimal("0.50");
    private static final BigDecimal DIAS_ANIO = new BigDecimal("365");
    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    public ResultadoCalculo calcular(Empleado empleado, LocalDate fechaCalculo, ParametrosCalculo params) {
        BigDecimal montoQuincena25 = calcularMonto(empleado, fechaCalculo, params);

        return ResultadoCalculo.builder()
                .salarioProporcional(BigDecimal.ZERO)
                .montoHorasExtraDiurnas(BigDecimal.ZERO)
                .montoHorasExtraNocturnas(BigDecimal.ZERO)
                .comisiones(BigDecimal.ZERO)
                .bonificaciones(BigDecimal.ZERO)
                .salarioBruto(montoQuincena25)
                .isss(BigDecimal.ZERO)
                .afp(BigDecimal.ZERO)
                .baseGravada(BigDecimal.ZERO)
                .isr(BigDecimal.ZERO)
                .descuentosVoluntarios(BigDecimal.ZERO)
                .salarioNeto(montoQuincena25)
                .aportePatronalIsss(BigDecimal.ZERO)
                .aportePatronalAfp(BigDecimal.ZERO)
                .diasDescontados(0)
                .diasPagoParcial(0)
                .porcentajePagoParcial(BigDecimal.ZERO)
                .horasDescontadas(BigDecimal.ZERO)
                .diasReportarIsssAfp(0)
                .diasDescansoTrabajados(0)
                .recargoDescansoTrabajado(BigDecimal.ZERO)
                .semanasConFaltaInjustificada(0)
                .descuentoSeptimoDia(BigDecimal.ZERO)
                .montoQuincena25(montoQuincena25)
                .build();
    }

    private BigDecimal calcularMonto(Empleado empleado, LocalDate fechaCalculo, ParametrosCalculo params) {
        if (!Boolean.TRUE.equals(params.getQuincena25Activa())) {
            return BigDecimal.ZERO;
        }
        if (empleado.getSalarioBase().compareTo(params.getQuincena25TopeSalario()) > 0) {
            return BigDecimal.ZERO;
        }
        if (empleado.getFechaIngreso() == null || !empleado.getFechaIngreso().isBefore(fechaCalculo)) {
            return BigDecimal.ZERO;
        }

        BigDecimal beneficioBase = empleado.getSalarioBase().multiply(PORCENTAJE_BENEFICIO);
        BigDecimal proporcion = calcularProporcionAntiguedad(empleado.getFechaIngreso(), fechaCalculo);
        return beneficioBase.multiply(proporcion).setScale(SCALE, RM);
    }

    private BigDecimal calcularProporcionAntiguedad(LocalDate fechaIngreso, LocalDate fechaCalculo) {
        if (!fechaIngreso.plusYears(1).isAfter(fechaCalculo)) {
            return BigDecimal.ONE;
        }
        long diasTrabajados = ChronoUnit.DAYS.between(fechaIngreso, fechaCalculo) + 1;
        if (diasTrabajados <= 0) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(diasTrabajados)
                .divide(DIAS_ANIO, 10, RM);
    }
}
