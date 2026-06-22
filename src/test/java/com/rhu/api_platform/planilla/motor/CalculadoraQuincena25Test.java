package com.rhu.api_platform.planilla.motor;

import com.rhu.api_platform.empleado.entity.Empleado;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Calculadora Quincena 25 — Pruebas unitarias")
class CalculadoraQuincena25Test {

    private CalculadoraQuincena25 calculadora;
    private ParametrosCalculo params;

    @BeforeEach
    void setUp() {
        calculadora = new CalculadoraQuincena25();
        params = ParametrosCalculo.builder()
                .quincena25Activa(true)
                .quincena25TopeSalario(new BigDecimal("1500.00"))
                .build();
    }

    private Empleado empleado(BigDecimal salario, LocalDate fechaIngreso) {
        Empleado e = new Empleado();
        e.setSalarioBase(salario);
        e.setFechaIngreso(fechaIngreso);
        return e;
    }

    @Test
    @DisplayName("Empleado con 1 año o más recibe 100% del beneficio")
    void empleadoConAnioCompleto() {
        LocalDate fechaCalculo = LocalDate.of(2026, 1, 25);
        Empleado e = empleado(new BigDecimal("1000.00"), LocalDate.of(2020, 5, 10));

        ResultadoCalculo r = calculadora.calcular(e, fechaCalculo, params);

        BigDecimal esperado = new BigDecimal("500.00");
        assertEquals(0, r.getMontoQuincena25().compareTo(esperado));
        assertEquals(0, r.getSalarioBruto().compareTo(esperado));
        assertEquals(0, r.getSalarioNeto().compareTo(esperado));
        assertEquals(0, r.getIsss().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.getAfp().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.getIsr().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Empleado con menos de 1 año recibe proporcional")
    void empleadoProporcional() {
        LocalDate fechaCalculo = LocalDate.of(2026, 1, 25);
        Empleado e = empleado(new BigDecimal("1200.00"), LocalDate.of(2025, 7, 1));

        ResultadoCalculo r = calculadora.calcular(e, fechaCalculo, params);

        // Beneficio base = 1200 * 50% = 600
        // Días trabajados = 1 de julio 2025 al 25 de enero 2026 = 209 días
        // Proporción = 209 / 365
        BigDecimal proporcion = new BigDecimal("209").divide(new BigDecimal("365"), 10, RoundingMode.HALF_UP);
        BigDecimal esperado = new BigDecimal("600.00").multiply(proporcion).setScale(2, RoundingMode.HALF_UP);

        assertTrue(r.getMontoQuincena25().compareTo(BigDecimal.ZERO) > 0);
        assertTrue(r.getMontoQuincena25().compareTo(new BigDecimal("600.00")) < 0);
        assertEquals(0, r.getSalarioBruto().compareTo(r.getMontoQuincena25()));
        assertEquals(0, r.getSalarioNeto().compareTo(r.getMontoQuincena25()));
    }

    @Test
    @DisplayName("Empleado con salario mayor al tope no recibe beneficio")
    void salarioMayorTope() {
        LocalDate fechaCalculo = LocalDate.of(2026, 1, 25);
        Empleado e = empleado(new BigDecimal("1600.00"), LocalDate.of(2020, 5, 10));

        ResultadoCalculo r = calculadora.calcular(e, fechaCalculo, params);

        assertEquals(0, r.getMontoQuincena25().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.getSalarioBruto().compareTo(BigDecimal.ZERO));
        assertEquals(0, r.getSalarioNeto().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Si Quincena 25 está desactivada no se calcula beneficio")
    void desactivada() {
        params.setQuincena25Activa(false);
        LocalDate fechaCalculo = LocalDate.of(2026, 1, 25);
        Empleado e = empleado(new BigDecimal("1000.00"), LocalDate.of(2020, 5, 10));

        ResultadoCalculo r = calculadora.calcular(e, fechaCalculo, params);

        assertEquals(0, r.getMontoQuincena25().compareTo(BigDecimal.ZERO));
    }

    @Test
    @DisplayName("Empleado que ingresa después de la fecha de cálculo no recibe beneficio")
    void ingresoPosterior() {
        LocalDate fechaCalculo = LocalDate.of(2026, 1, 25);
        Empleado e = empleado(new BigDecimal("1000.00"), LocalDate.of(2026, 2, 1));

        ResultadoCalculo r = calculadora.calcular(e, fechaCalculo, params);

        assertEquals(0, r.getMontoQuincena25().compareTo(BigDecimal.ZERO));
    }
}
