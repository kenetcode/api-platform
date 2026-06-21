package com.rhu.api_platform.empleado.dto;

import com.rhu.api_platform.empleado.entity.EstadoEmpleado;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CambioEstadoRequest {
    @NotNull(message = "El estado es obligatorio")
    private EstadoEmpleado estado;
}
