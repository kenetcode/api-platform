package com.rhu.api_platform.ausencia.dto;

import com.rhu.api_platform.ausencia.entity.TipoIncapacidad;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDate;

@Data
@Builder
public class IncapacidadResponse {
    private Long id;
    private Long empleadoId;
    private String nombreEmpleado;
    private TipoIncapacidad tipo;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer dias;
    private String documentoUrl;
}
