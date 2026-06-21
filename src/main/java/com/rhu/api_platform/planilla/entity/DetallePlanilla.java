package com.rhu.api_platform.planilla.entity;

import com.rhu.api_platform.empleado.entity.Empleado;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "detalles_planilla")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetallePlanilla {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "planilla_id", nullable = false)
    private Planilla planilla;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "empleado_id", nullable = false)
    private Empleado empleado;

    @Column(name = "dias_laborados", nullable = false)
    @Builder.Default
    private Integer diasLaborados = 30;

    @Column(name = "horas_extra_diurnas", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal horasExtraDiurnas = BigDecimal.ZERO;

    @Column(name = "horas_extra_nocturnas", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal horasExtraNocturnas = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal comisiones = BigDecimal.ZERO;

    @Column(precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal bonificaciones = BigDecimal.ZERO;

    @Column(name = "descuentos_voluntarios", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal descuentosVoluntarios = BigDecimal.ZERO;

    // Resultados calculados
    @Column(name = "salario_bruto", precision = 12, scale = 2)
    private BigDecimal salarioBruto;

    @Column(precision = 10, scale = 2)
    private BigDecimal isss;

    @Column(precision = 10, scale = 2)
    private BigDecimal afp;

    @Column(precision = 10, scale = 2)
    private BigDecimal isr;

    @Column(name = "salario_neto", precision = 12, scale = 2)
    private BigDecimal salarioNeto;

    @Column(name = "aporte_patronal_isss", precision = 10, scale = 2)
    private BigDecimal aportePatronalIsss;

    @Column(name = "aporte_patronal_afp", precision = 10, scale = 2)
    private BigDecimal aportePatronalAfp;
}
