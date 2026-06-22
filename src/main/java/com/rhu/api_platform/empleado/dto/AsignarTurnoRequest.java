package com.rhu.api_platform.empleado.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AsignarTurnoRequest {

    @NotNull(message = "El ID del turno es obligatorio")
    private Long turnoId;
}
