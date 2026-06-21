package com.rhu.api_platform.planilla.motor;

import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;

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
     * Calcula el salario neto completo para un empleado.
     *
     * @param salarioBase           salario base mensual del empleado
     * @param diasLaborados         días trabajados en el período
     * @param horasExtraDiurnas     horas extra diurnas
     * @param horasExtraNocturnas   horas extra nocturnas
     * @param comisiones            comisiones del período
     * @param bonificaciones        bonificaciones del período
     * @param descuentosVoluntarios descuentos adicionales voluntarios
     * @param params                parámetros legales vigentes
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

        // 1. Salario proporcional por días laborados
        BigDecimal salarioProporcional = salarioBase
                .multiply(new BigDecimal(diasLaborados))
                .divide(DIAS_MES, SCALE, RM);

        // 2. Valor de la hora ordinaria
        BigDecimal valorHora = salarioBase
                .divide(DIAS_MES, 10, RM)
                .divide(HORAS_DIA, 10, RM);

        // Horas extra diurnas: ×2
        BigDecimal montoExtraDiurna = valorHora
                .multiply(new BigDecimal("2"))
                .multiply(coalesce(horasExtraDiurnas))
                .setScale(SCALE, RM);

        // Horas extra nocturnas: ×2.25
        BigDecimal montoExtraNocturna = valorHora
                .multiply(new BigDecimal("2.25"))
                .multiply(coalesce(horasExtraNocturnas))
                .setScale(SCALE, RM);

        // 3. Salario bruto
        BigDecimal salarioBruto = salarioProporcional
                .add(montoExtraDiurna)
                .add(montoExtraNocturna)
                .add(coalesce(comisiones))
                .add(coalesce(bonificaciones))
                .setScale(SCALE, RM);

        // 4. ISSS trabajador: 3% sobre base máx $1,000 → tope $30
        BigDecimal baseIsss = salarioBruto.min(params.getIsssBasMaxima());
        BigDecimal isss = baseIsss.multiply(params.getIsssPorc()).setScale(SCALE, RM);

        // 5. AFP trabajador: 7.25% sin tope
        BigDecimal afp = salarioBruto.multiply(params.getAfpPorc()).setScale(SCALE, RM);

        // 6. Base gravada ISR
        BigDecimal baseGravada = salarioBruto.subtract(isss).subtract(afp).setScale(SCALE, RM);

        // 7. ISR por tramos
        BigDecimal isr = calcularIsr(baseGravada, params);

        // 8. Salario neto
        BigDecimal neto = salarioBruto
                .subtract(isss)
                .subtract(afp)
                .subtract(isr)
                .subtract(coalesce(descuentosVoluntarios))
                .setScale(SCALE, RM);

        // 9. Aportes patronales
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
                .build();
    }

    /**
     * Calcula el ISR mensual según tabla de tramos de El Salvador 2026.
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
     */
    public BigDecimal calcularAguinaldo(BigDecimal salarioBase, int aniosAntiguedad) {
        BigDecimal valorDia = salarioBase.divide(DIAS_MES, 10, RM);
        int dias;
        if (aniosAntiguedad < 1) {
            dias = 0;
        } else if (aniosAntiguedad < 3) {
            dias = 15;
        } else if (aniosAntiguedad < 10) {
            dias = 19;
        } else {
            dias = 21;
        }
        return valorDia.multiply(new BigDecimal(dias)).setScale(SCALE, RM);
    }

    private BigDecimal coalesce(BigDecimal v) {
        return v == null ? BigDecimal.ZERO : v;
    }
}
