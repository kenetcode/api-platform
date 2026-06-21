package com.rhu.api_platform.ausencia.dto;

import com.rhu.api_platform.ausencia.entity.TipoAusencia;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class AusenciaResponse {
    private Long id;
    private Long empleadoId;
    private String nombreEmpleado;
    private TipoAusencia tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Boolean justificada;
    private String observacion;
}
