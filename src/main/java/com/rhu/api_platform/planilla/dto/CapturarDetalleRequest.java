package com.rhu.api_platform.planilla.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import java.math.BigDecimal;

@Data
public class CapturarDetalleRequest {
    @NotNull(message = "El empleado es obligatorio")
    private Long empleadoId;

    @Min(value = 0, message = "Los días laborados no pueden ser negativos")
    private int diasLaborados = 30;

    private BigDecimal horasExtraDiurnas;
    private BigDecimal horasExtraNocturnas;
    private BigDecimal comisiones;
    private BigDecimal bonificaciones;
    private BigDecimal descuentosVoluntarios;

    @Min(value = 0, message = "Los días de descanso trabajados no pueden ser negativos")
    private int diasDescansoTrabajados = 0;
}
