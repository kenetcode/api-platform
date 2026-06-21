package com.rhu.api_platform.empresa.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DashboardResponse {
    private Long totalEmpleadosActivos;
    private Long totalEmpleadosInactivos;
    private String ultimoPeriodo;
    private BigDecimal totalBrutoUltimoPeriodo;
    private BigDecimal totalNetoUltimoPeriodo;
    private BigDecimal totalDeduccionesUltimoPeriodo;
}
