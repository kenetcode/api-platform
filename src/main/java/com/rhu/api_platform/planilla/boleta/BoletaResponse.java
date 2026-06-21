package com.rhu.api_platform.planilla.boleta;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class BoletaResponse {
    private String empresa;
    private String periodoMes;
    private String tipoPlanilla;
    private LocalDateTime fechaGeneracion;
    private Long empleadoId;
    private String nombreCompleto;
    private String dui;
    private String cargo;
    private String departamento;
    private String afp;
    private Integer diasLaborados;
    private BigDecimal salarioBase;
    private BigDecimal salarioProporcional;
    private BigDecimal horasExtraDiurnas;
    private BigDecimal horasExtraNocturnas;
    private BigDecimal comisiones;
    private BigDecimal bonificaciones;
    private BigDecimal totalPercepciones;
    private BigDecimal isss;
    private BigDecimal afpMonto;
    private BigDecimal isr;
    private BigDecimal descuentosVoluntarios;
    private BigDecimal totalDeducciones;
    private BigDecimal salarioNeto;
    private BigDecimal aportePatronalIsss;
    private BigDecimal aportePatronalAfp;
    private BigDecimal totalAportePatronal;
}
