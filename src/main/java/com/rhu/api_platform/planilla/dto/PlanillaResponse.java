package com.rhu.api_platform.planilla.dto;

import com.rhu.api_platform.planilla.entity.EstadoPlanilla;
import com.rhu.api_platform.planilla.entity.TipoPlanilla;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PlanillaResponse {
    private Long id;
    private String periodoMes;
    private TipoPlanilla tipo;
    private EstadoPlanilla estado;
    private BigDecimal totalBruto;
    private BigDecimal totalIsss;
    private BigDecimal totalAfp;
    private BigDecimal totalIsr;
    private BigDecimal totalNeto;
    private LocalDateTime creadoEn;
    private LocalDateTime aprobadoEn;
    private List<DetallePlanillaResponse> detalles;
}
