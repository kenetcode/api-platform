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

    // Desglose de ausencias/incapacidades aplicado
    @Column(name = "dias_descontados")
    @Builder.Default
    private Integer diasDescontados = 0;

    @Column(name = "dias_pago_parcial")
    @Builder.Default
    private Integer diasPagoParcial = 0;

    @Column(name = "porcentaje_pago_parcial", precision = 5, scale = 2)
    @Builder.Default
    private BigDecimal porcentajePagoParcial = BigDecimal.ZERO;

    @Column(name = "horas_descontadas", precision = 8, scale = 2)
    @Builder.Default
    private BigDecimal horasDescontadas = BigDecimal.ZERO;

    @Column(name = "dias_reportar_isss_afp")
    @Builder.Default
    private Integer diasReportarIsssAfp = 0;

    @Column(name = "dias_descanso_trabajados")
    @Builder.Default
    private Integer diasDescansoTrabajados = 0;

    @Column(name = "recargo_descanso_trabajado", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal recargoDescansoTrabajado = BigDecimal.ZERO;

    @Column(name = "descuento_septimo_dia", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal descuentoSeptimoDia = BigDecimal.ZERO;

    @Column(name = "semanas_con_falta_injustificada")
    @Builder.Default
    private Integer semanasConFaltaInjustificada = 0;

    @Column(name = "monto_quincena_25", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal montoQuincena25 = BigDecimal.ZERO;

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
