package com.rhu.api_platform.planilla.dto;

import com.rhu.api_platform.planilla.entity.TipoPlanilla;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

@Data
public class CrearPlanillaRequest {
    @NotBlank(message = "El período es obligatorio")
    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])$", message = "El período debe tener formato YYYY-MM")
    private String periodoMes;

    @NotNull(message = "El tipo de planilla es obligatorio")
    private TipoPlanilla tipo;
}
