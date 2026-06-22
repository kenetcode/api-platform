package com.rhu.api_platform.planilla.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DetallePlanillaResponse {
    private Long id;
    private Long empleadoId;
    private String nombreEmpleado;
    private Integer diasLaborados;
    private BigDecimal horasExtraDiurnas;
    private BigDecimal horasExtraNocturnas;
    private BigDecimal comisiones;
    private BigDecimal bonificaciones;
    private BigDecimal descuentosVoluntarios;

    private Integer diasDescontados;
    private Integer diasPagoParcial;
    private BigDecimal porcentajePagoParcial;
    private BigDecimal horasDescontadas;
    private Integer diasReportarIsssAfp;

    private Integer diasDescansoTrabajados;
    private BigDecimal recargoDescansoTrabajado;
    private Integer semanasConFaltaInjustificada;
    private BigDecimal descuentoSeptimoDia;

    private BigDecimal montoQuincena25;

    private BigDecimal salarioBruto;
    private BigDecimal isss;
    private BigDecimal afp;
    private BigDecimal isr;
    private BigDecimal salarioNeto;
    private BigDecimal aportePatronalIsss;
    private BigDecimal aportePatronalAfp;
}
