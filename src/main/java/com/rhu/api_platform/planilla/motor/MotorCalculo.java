package com.rhu.api_platform.planilla.motor;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.Period;
import java.time.temporal.ChronoUnit;

/**
 * Motor de cálculo de nómina.
 * TODOS los montos usan BigDecimal con HALF_UP a 2 decimales.
 * Implementa la secuencia de cálculo del Plan de Implementación §9.2.
 */
@Service
public class MotorCalculo {

    private static final BigDecimal DIAS_MES = new BigDecimal("30");
    private static final BigDecimal HORAS_DIA = new BigDecimal("8");
    private static final int SCALE = 2;
    private static final RoundingMode RM = RoundingMode.HALF_UP;

    /**
     * Calcula el salario neto completo para un empleado (sin ausencias ni recargos de descanso).
     */
    public ResultadoCalculo calcular(
            BigDecimal salarioBase,
            int diasLaborados,
            BigDecimal horasExtraDiurnas,
            BigDecimal horasExtraNocturnas,
            BigDecimal comisiones,
            BigDecimal bonificaciones,
            BigDecimal descuentosVoluntarios,
            ParametrosCalculo params) {
        return calcular(salarioBase, diasLaborados, horasExtraDiurnas, horasExtraNocturnas,
                comisiones, bonificaciones, descuentosVoluntarios, params,
                ResumenAusencias.builder().build(), 0);
    }

    /**
     * Calcula el salario neto completo para un empleado, aplicando ausencias/incapacidades,
     * recargo por días de descanso trabajados y descuento de séptimo día por faltas injustificadas.
     */
    public ResultadoCalculo calcular(
            BigDecimal salarioBase,
            int diasLaborados,
            BigDecimal horasExtraDiurnas,
            BigDecimal horasExtraNocturnas,
            BigDecimal comisiones,
            BigDecimal bonificaciones,
            BigDecimal descuentosVoluntarios,
            ParametrosCalculo params,
            ResumenAusencias ausencias,
            int diasDescansoTrabajados) {

        // 1. Valor de la hora ordinaria
        BigDecimal valorHora = salarioBase
                .divide(DIAS_MES, 10, RM)
                .divide(HORAS_DIA, 10, RM);

        // 2. Salario proporcional por días laborados, ajustado por ausencias
        int diasEfectivos = Math.max(0, diasLaborados - ausencias.getDiasDescontar());
        BigDecimal salarioProporcional = salarioBase
                .multiply(new BigDecimal(diasEfectivos))
                .divide(DIAS_MES, SCALE, RM);

        // Pago parcial por incapacidad común días 1-3
        BigDecimal pagoParcial = BigDecimal.ZERO;
        if (ausencias.getDiasPagoParcial() > 0 && ausencias.getPorcentajePagoParcial().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal proporcion = ausencias.getPorcentajePagoParcial()
                    .divide(new BigDecimal("100"), 10, RM);
            pagoParcial = salarioBase
                    .multiply(new BigDecimal(ausencias.getDiasPagoParcial()))
                    .divide(DIAS_MES, 10, RM)
                    .multiply(proporcion)
                    .setScale(SCALE, RM);
        }

        // Descuento por ausencias por horas
        BigDecimal descuentoHoras = BigDecimal.ZERO;
        if (ausencias.getHorasDescontar().compareTo(BigDecimal.ZERO) > 0) {
            descuentoHoras = valorHora.multiply(ausencias.getHorasDescontar()).setScale(SCALE, RM);
        }

        // 3. Recargo por día de descanso trabajado: 50% adicional sobre el valor del día
        BigDecimal valorDia = salarioBase.divide(DIAS_MES, 10, RM);
        BigDecimal recargoDescansoTrabajado = valorDia
                .multiply(new BigDecimal("0.5"))
                .multiply(new BigDecimal(Math.max(0, diasDescansoTrabajados)))
                .setScale(SCALE, RM);

        // 4. Descuento de séptimo día por falta injustificada: 1 día por cada semana con falta
        BigDecimal descuentoSeptimoDia = valorDia
                .multiply(new BigDecimal(ausencias.getSemanasConFaltaInjustificada()))
                .setScale(SCALE, RM);

        // 5. Horas extra diurnas: ×2
        BigDecimal montoExtraDiurna = valorHora
                .multiply(new BigDecimal("2"))
                .multiply(coalesce(horasExtraDiurnas))
                .setScale(SCALE, RM);

        // 4. Horas extra nocturnas: ×2.25
        BigDecimal montoExtraNocturna = valorHora
                .multiply(new BigDecimal("2.25"))
                .multiply(coalesce(horasExtraNocturnas))
                .setScale(SCALE, RM);

        // 6. Salario bruto
        BigDecimal salarioBruto = salarioProporcional
                .add(pagoParcial)
                .subtract(descuentoHoras)
                .add(recargoDescansoTrabajado)
                .subtract(descuentoSeptimoDia)
                .add(montoExtraDiurna)
                .add(montoExtraNocturna)
                .add(coalesce(comisiones))
                .add(coalesce(bonificaciones))
                .setScale(SCALE, RM);

        // 6. ISSS trabajador: 3% sobre base máx $1,000 → tope $30
        BigDecimal baseIsss = salarioBruto.min(params.getIsssBasMaxima());
        BigDecimal isss = baseIsss.multiply(params.getIsssPorc()).setScale(SCALE, RM);

        // 7. AFP trabajador: 7.25% sin tope
        BigDecimal afp = salarioBruto.multiply(params.getAfpPorc()).setScale(SCALE, RM);

        // 8. Base gravada ISR
        BigDecimal baseGravada = salarioBruto.subtract(isss).subtract(afp).setScale(SCALE, RM);

        // 9. ISR por tramos
        BigDecimal isr = calcularIsr(baseGravada, params);

        // 10. Salario neto
        BigDecimal neto = salarioBruto
                .subtract(isss)
                .subtract(afp)
                .subtract(isr)
                .subtract(coalesce(descuentosVoluntarios))
                .setScale(SCALE, RM);

        // 11. Aportes patronales
        BigDecimal baseAportePatronalIsss = salarioBruto.min(params.getIsssBasMaxima());
        BigDecimal aportePatronalIsss = baseAportePatronalIsss
                .multiply(params.getAportePatronalIsssPorc())
                .setScale(SCALE, RM);
        BigDecimal aportePatronalAfp = salarioBruto
                .multiply(params.getAportePatronalAfpPorc())
                .setScale(SCALE, RM);

        return ResultadoCalculo.builder()
                .salarioProporcional(salarioProporcional)
                .montoHorasExtraDiurnas(montoExtraDiurna)
                .montoHorasExtraNocturnas(montoExtraNocturna)
                .comisiones(coalesce(comisiones))
                .bonificaciones(coalesce(bonificaciones))
                .salarioBruto(salarioBruto)
                .isss(isss)
                .afp(afp)
                .baseGravada(baseGravada)
                .isr(isr)
                .descuentosVoluntarios(coalesce(descuentosVoluntarios))
                .salarioNeto(neto)
                .aportePatronalIsss(aportePatronalIsss)
                .aportePatronalAfp(aportePatronalAfp)
                .diasDescontados(ausencias.getDiasDescontar())
                .diasPagoParcial(ausencias.getDiasPagoParcial())
                .porcentajePagoParcial(ausencias.getPorcentajePagoParcial())
                .horasDescontadas(ausencias.getHorasDescontar())
                .diasReportarIsssAfp(ausencias.getDiasReportarIsssAfp())
                .diasDescansoTrabajados(diasDescansoTrabajados)
                .recargoDescansoTrabajado(recargoDescansoTrabajado)
                .semanasConFaltaInjustificada(ausencias.getSemanasConFaltaInjustificada())
                .descuentoSeptimoDia(descuentoSeptimoDia)
                .build();
    }

    /**
     * Calcula el ISR quincenal según tabla de tramos de El Salvador 2026.
     */
    public BigDecimal calcularIsr(BigDecimal baseGravada, ParametrosCalculo params) {
        if (baseGravada.compareTo(params.getIsrExento()) <= 0) {
            return BigDecimal.ZERO;
        }
        if (baseGravada.compareTo(params.getIsrTramo2Fin()) <= 0) {
            // Tramo II: 10% sobre exceso de $550.00 + cuota $17.67
            return baseGravada.subtract(params.getIsrExento())
                    .multiply(params.getIsrTramo2Porc())
                    .add(params.getIsrTramo2Cuota())
                    .setScale(SCALE, RM);
        }
        if (baseGravada.compareTo(params.getIsrTramo3Fin()) <= 0) {
            // Tramo III: 20% sobre exceso de $895.24 + cuota $60.00
            return baseGravada.subtract(params.getIsrTramo3Inicio().subtract(new BigDecimal("0.01")))
                    .multiply(params.getIsrTramo3Porc())
                    .add(params.getIsrTramo3Cuota())
                    .setScale(SCALE, RM);
        }
        // Tramo IV: 30% sobre exceso de $2,038.10 + cuota $288.57
        return baseGravada.subtract(params.getIsrTramo4Inicio().subtract(new BigDecimal("0.01")))
                .multiply(params.getIsrTramo4Porc())
                .add(params.getIsrTramo4Cuota())
                .setScale(SCALE, RM);
    }

    /**
     * Calcula la proyección de vacaciones: 15 días de salario + 30% de prima.
     */
    public BigDecimal calcularVacaciones(BigDecimal salarioBase) {
        BigDecimal valorDia = salarioBase.divide(DIAS_MES, 10, RM);
        BigDecimal quinceDias = valorDia.multiply(new BigDecimal("15")).setScale(SCALE, RM);
        BigDecimal prima = quinceDias.multiply(new BigDecimal("0.30")).setScale(SCALE, RM);
        return quinceDias.add(prima);
    }

    /**
     * Calcula el aguinaldo según años de antigüedad.
     * 1-<3 años: 15 días; 3-<10 años: 19 días; >=10 años: 21 días.
     * Si tiene menos de 1 año se calcula proporcionalmente a los días trabajados.
     */
    public BigDecimal calcularAguinaldo(BigDecimal salarioBase, LocalDate fechaIngreso, LocalDate fechaCalculo) {
        if (fechaIngreso == null || !fechaIngreso.isBefore(fechaCalculo)) {
            return BigDecimal.ZERO;
        }

        BigDecimal valorDia = salarioBase.divide(DIAS_MES, 10, RM);
        int aniosCompletos = Period.between(fechaIngreso, fechaCalculo).getYears();

        BigDecimal diasAguinaldo;
        if (aniosCompletos < 1) {
            long diasTrabajados = ChronoUnit.DAYS.between(fechaIngreso, fechaCalculo) + 1;
            BigDecimal proporcion = new BigDecimal(diasTrabajados)
                    .divide(new BigDecimal("365"), 10, RM);
            diasAguinaldo = new BigDecimal("15").multiply(proporcion);
        } else if (aniosCompletos < 3) {
            diasAguinaldo = new BigDecimal("15");
        } else if (aniosCompletos < 10) {
            diasAguinaldo = new BigDecimal("19");
        } else {
            diasAguinaldo = new BigDecimal("21");
        }

        return valorDia.multiply(diasAguinaldo).setScale(SCALE, RM);
    }

    private BigDecimal coalesce(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
