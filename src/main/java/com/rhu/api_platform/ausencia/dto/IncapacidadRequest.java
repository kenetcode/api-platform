package com.rhu.api_platform.ausencia.dto;

import com.rhu.api_platform.ausencia.entity.TipoIncapacidad;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import java.time.LocalDate;

@Data
public class IncapacidadRequest {
    @NotNull(message = "El tipo de incapacidad es obligatorio")
    private TipoIncapacidad tipo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    @NotNull(message = "Los días son obligatorios")
    @Positive(message = "Los días deben ser positivos")
    private Integer dias;

    private String documentoUrl;
}
