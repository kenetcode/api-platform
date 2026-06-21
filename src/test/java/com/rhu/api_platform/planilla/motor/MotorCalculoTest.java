package com.rhu.api_platform.planilla.motor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.math.RoundingMode;
import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Motor de Cálculo — Pruebas unitarias")
class MotorCalculoTest {

    private MotorCalculo motor;
    private ParametrosCalculo params;

    @BeforeEach
    void setUp() {
        motor = new MotorCalculo();
        // Parámetros legales El Salvador 2026
        params = ParametrosCalculo.builder()
                .isssPorc(new BigDecimal("0.03"))
                .isssBasMaxima(new BigDecimal("1000.00"))
                .aportePatronalIsssPorc(new BigDecimal("0.075"))
                .afpPorc(new BigDecimal("0.0725"))
                .aportePatronalAfpPorc(new BigDecimal("0.0875"))
                .isrExento(new BigDecimal("550.00"))
                .isrTramo2Inicio(new BigDecimal("550.01"))
                .isrTramo2Fin(new BigDecimal("895.24"))
                .isrTramo2Porc(new BigDecimal("0.10"))
                .isrTramo2Cuota(new BigDecimal("17.67"))
                .isrTramo3Inicio(new BigDecimal("895.25"))
                .isrTramo3Fin(new BigDecimal("2038.10"))
                .isrTramo3Porc(new BigDecimal("0.20"))
                .isrTramo3Cuota(new BigDecimal("60.00"))
                .isrTramo4Inicio(new BigDecimal("2038.11"))
                .isrTramo4Porc(new BigDecimal("0.30"))
                .isrTramo4Cuota(new BigDecimal("288.57"))
                .horasMensuales(new BigDecimal("240"))
                .build();
    }

    // ===== ISR =====

    @Test
    @DisplayName("ISR Tramo I — base gravada <= $550 → ISR = $0")
    void isrTramo1_exento() {
        BigDecimal base = new BigDecimal("550.00");
        BigDecimal isr = motor.calcularIsr(base, params);
        assertEquals(0, isr.compareTo(BigDecimal.ZERO), "ISR debe ser $0.00 en tramo exento");
    }

    @Test
    @DisplayName("ISR Tramo I — base exactamente $549.99 → ISR = $0")
    void isrTramo1_menosDeExento() {
        BigDecimal isr = motor.calcularIsr(new BigDecimal("549.99"), params);
        assertEquals(0, isr.compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("ISR Tramo II — base $600 → (600-550)*10% + $17.67 = $22.67")
    void isrTramo2() {
        BigDecimal isr = motor.calcularIsr(new BigDecimal("600.00"), params);
        BigDecimal esperado = new BigDecimal("22.67");
        assertEquals(0, isr.compareTo(esperado),
                "ISR Tramo II esperado $22.67 pero fue $" + isr);
    }

    @Test
    @DisplayName("ISR Tramo II límite inferior — base $550.01")
    void isrTramo2_limiteInferior() {
        BigDecimal isr = motor.calcularIsr(new BigDecimal("550.01"), params);
        assertTrue(isr.compareTo(BigDecimal.ZERO) > 0, "ISR debe ser > 0 sobre el exento");
    }

    @Test
    @DisplayName("ISR Tramo III — base $1000")
    void isrTramo3() {
        // (1000 - 895.24) * 20% + 60.00
        BigDecimal base = new BigDecimal("1000.00");
        BigDecimal esperado = new BigDecimal("895.24")
                .negate().add(base)
                .multiply(new BigDecimal("0.20"))
                .add(new BigDecimal("60.00"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal isr = motor.calcularIsr(base, params);
        assertEquals(0, isr.compareTo(esperado),
                "ISR Tramo III esperado $" + esperado + " pero fue $" + isr);
    }

    @Test
    @DisplayName("ISR Tramo IV — base $3000")
    void isrTramo4() {
        // (3000 - 2038.10) * 30% + 288.57
        BigDecimal base = new BigDecimal("3000.00");
        BigDecimal esperado = base.subtract(new BigDecimal("2038.10"))
                .multiply(new BigDecimal("0.30"))
                .add(new BigDecimal("288.57"))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal isr = motor.calcularIsr(base, params);
        assertEquals(0, isr.compareTo(esperado),
                "ISR Tramo IV esperado $" + esperado + " pero fue $" + isr);
    }

    // ===== ISSS =====

    @Test
    @DisplayName("ISSS — salario $408.80 → 3% = $12.26")
    void isss_salarioMinimo() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("408.80"), 30,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        BigDecimal esperado = new BigDecimal("408.80").multiply(new BigDecimal("0.03"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, r.getIsss().compareTo(esperado),
                "ISSS esperado $" + esperado + " pero fue $" + r.getIsss());
    }

    @Test
    @DisplayName("ISSS — salario $1500 → base tope $1000 → ISSS = $30.00")
    void isss_tope() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("1500.00"), 30,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        assertEquals(0, r.getIsss().compareTo(new BigDecimal("30.00")),
                "ISSS debe ser exactamente $30.00 con salario $1500. Fue: $" + r.getIsss());
    }

    @Test
    @DisplayName("ISSS — salario $1000 → exactamente en el tope → ISSS = $30.00")
    void isss_exactamenteTope() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("1000.00"), 30,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        assertEquals(0, r.getIsss().compareTo(new BigDecimal("30.00")));
    }

    // ===== AFP =====

    @Test
    @DisplayName("AFP — sin tope, 7.25% sobre salario real $2000")
    void afp_sinTope() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("2000.00"), 30,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        BigDecimal esperado = new BigDecimal("2000.00").multiply(new BigDecimal("0.0725"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, r.getAfp().compareTo(esperado),
                "AFP esperado $" + esperado + " pero fue $" + r.getAfp());
    }

    // ===== Horas extra =====

    @Test
    @DisplayName("Horas extra diurnas — salario $408.80, 10 h.e.d. → monto = valorHora × 2 × 10")
    void horasExtraDiurnas() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("408.80"), 30,
                new BigDecimal("10"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        // valorHora = 408.80 / 30 / 8 = 1.7033...
        // monto = 1.7033 × 2 × 10 = 34.07 aprox
        assertTrue(r.getMontoHorasExtraDiurnas().compareTo(BigDecimal.ZERO) > 0,
                "Monto H.E. diurnas debe ser > 0");
        // Verifica que bruto > salarioProporcional
        assertTrue(r.getSalarioBruto().compareTo(r.getSalarioProporcional()) > 0);
    }

    @Test
    @DisplayName("Horas extra nocturnas — recargo ×2.25 > ×2 diurnas")
    void horasExtraNocturnas_mayorQuesDiurnas() {
        ResultadoCalculo rDiurnas = motor.calcular(
                new BigDecimal("408.80"), 30,
                new BigDecimal("10"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        ResultadoCalculo rNocturnas = motor.calcular(
                new BigDecimal("408.80"), 30,
                BigDecimal.ZERO, new BigDecimal("10"),
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        assertTrue(rNocturnas.getMontoHorasExtraNocturnas()
                .compareTo(rDiurnas.getMontoHorasExtraDiurnas()) > 0,
                "H.E. nocturnas (×2.25) deben ser mayores que diurnas (×2) para el mismo número de horas");
    }

    // ===== Cálculo completo =====

    @Test
    @DisplayName("Cálculo completo — salario $800, 30 días — verificar neto")
    void calculoCompleto_salario800() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("800.00"), 30,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);

        // Bruto = 800.00 (30/30 días)
        assertEquals(0, r.getSalarioBruto().compareTo(new BigDecimal("800.00")));
        // ISSS = 800 × 3% = 24.00
        assertEquals(0, r.getIsss().compareTo(new BigDecimal("24.00")));
        // AFP = 800 × 7.25% = 58.00
        assertEquals(0, r.getAfp().compareTo(new BigDecimal("58.00")));
        // Base gravada = 800 - 24 - 58 = 718.00
        assertEquals(0, r.getBaseGravada().compareTo(new BigDecimal("718.00")));
        // Neto = Bruto - ISSS - AFP - ISR
        BigDecimal netoEsperado = r.getSalarioBruto()
                .subtract(r.getIsss()).subtract(r.getAfp()).subtract(r.getIsr());
        assertEquals(0, r.getSalarioNeto().compareTo(netoEsperado));
    }

    @Test
    @DisplayName("Cálculo días proporcionales — 15 días sobre salario $600")
    void calculoDiasProporcionales() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("600.00"), 15,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        BigDecimal esperado = new BigDecimal("300.00");
        assertEquals(0, r.getSalarioProporcional().compareTo(esperado),
                "15/30 días de $600 = $300. Fue: $" + r.getSalarioProporcional());
    }

    @Test
    @DisplayName("Aportes patronales — ISSS 7.5% tope $75, AFP 8.75% sin tope")
    void aportesPatronales() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("800.00"), 30,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        // ISSS patronal = 800 × 7.5% = 60.00
        assertEquals(0, r.getAportePatronalIsss().compareTo(new BigDecimal("60.00")));
        // AFP patronal = 800 × 8.75% = 70.00
        assertEquals(0, r.getAportePatronalAfp().compareTo(new BigDecimal("70.00")));
    }

    @Test
    @DisplayName("ISSS patronal tope — salario $1500 → base máx $1000 → $75.00")
    void isssPatronalTope() {
        ResultadoCalculo r = motor.calcular(
                new BigDecimal("1500.00"), 30,
                BigDecimal.ZERO, BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, params);
        assertEquals(0, r.getAportePatronalIsss().compareTo(new BigDecimal("75.00")),
                "ISSS patronal tope debe ser $75.00. Fue: $" + r.getAportePatronalIsss());
    }

    // ===== Prestaciones =====

    @Test
    @DisplayName("Vacaciones — salario $408.80 → 15 días + 30% prima")
    void vacaciones() {
        BigDecimal vacaciones = motor.calcularVacaciones(new BigDecimal("408.80"));
        // 15 días = 408.80/30 × 15 = 204.40; prima 30% = 61.32; total = 265.72
        BigDecimal valorDia = new BigDecimal("408.80").divide(new BigDecimal("30"), 10, RoundingMode.HALF_UP);
        BigDecimal quinceDias = valorDia.multiply(new BigDecimal("15")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal prima = quinceDias.multiply(new BigDecimal("0.30")).setScale(2, RoundingMode.HALF_UP);
        BigDecimal esperado = quinceDias.add(prima);
        assertEquals(0, vacaciones.compareTo(esperado),
                "Vacaciones esperado $" + esperado + " pero fue $" + vacaciones);
    }

    @Test
    @DisplayName("Aguinaldo — 1 a <3 años → 15 días")
    void aguinaldo_1a3anios() {
        BigDecimal ag = motor.calcularAguinaldo(new BigDecimal("408.80"), 2);
        BigDecimal esperado = new BigDecimal("408.80")
                .divide(new BigDecimal("30"), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("15"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, ag.compareTo(esperado));
    }

    @Test
    @DisplayName("Aguinaldo — 3 a <10 años → 19 días")
    void aguinaldo_3a10anios() {
        BigDecimal ag = motor.calcularAguinaldo(new BigDecimal("600.00"), 5);
        BigDecimal esperado = new BigDecimal("600.00")
                .divide(new BigDecimal("30"), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("19"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, ag.compareTo(esperado));
    }

    @Test
    @DisplayName("Aguinaldo — >=10 años → 21 días")
    void aguinaldo_mas10anios() {
        BigDecimal ag = motor.calcularAguinaldo(new BigDecimal("800.00"), 12);
        BigDecimal esperado = new BigDecimal("800.00")
                .divide(new BigDecimal("30"), 10, RoundingMode.HALF_UP)
                .multiply(new BigDecimal("21"))
                .setScale(2, RoundingMode.HALF_UP);
        assertEquals(0, ag.compareTo(esperado));
    }

    @Test
    @DisplayName("Aguinaldo — <1 año → $0")
    void aguinaldo_menosDeAnio() {
        BigDecimal ag = motor.calcularAguinaldo(new BigDecimal("408.80"), 0);
        assertEquals(0, ag.compareTo(BigDecimal.ZERO));
    }
}
