package com.rhu.api_platform.ausencia.dto;

import com.rhu.api_platform.ausencia.entity.TipoAusencia;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class AusenciaRequest {

    @NotNull(message = "El tipo de ausencia es obligatorio")
    private TipoAusencia tipo;

    @NotNull(message = "La fecha de inicio es obligatoria")
    private LocalDate fechaInicio;

    @NotNull(message = "La fecha de fin es obligatoria")
    private LocalDate fechaFin;

    private Integer dias;

    private BigDecimal horas;

    private String documentoRespaldoUrl;

    private BigDecimal pagoPorcentaje;

    private String observacion;
}
