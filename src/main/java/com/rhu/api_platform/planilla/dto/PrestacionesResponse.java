package com.rhu.api_platform.planilla.dto;

import lombok.Builder;
import lombok.Data;
import java.math.BigDecimal;

@Data
@Builder
public class PrestacionesResponse {
    private Long empleadoId;
    private String nombreEmpleado;
    private String tipo;
    private BigDecimal monto;
    private String descripcion;
}
