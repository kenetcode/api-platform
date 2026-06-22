package com.rhu.api_platform.ausencia.dto;

import com.rhu.api_platform.ausencia.entity.TipoAusencia;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
public class AusenciaResponse {
    private Long id;
    private Long empleadoId;
    private String nombreEmpleado;
    private TipoAusencia tipo;
    private String tipoDescripcion;
    private LocalDate fechaInicio;
    private LocalDate fechaFin;
    private Integer dias;
    private BigDecimal horas;
    private String documentoRespaldoUrl;
    private BigDecimal pagoPorcentaje;
    private Boolean conGoceSueldo;
    private Boolean afectaSeptimoDia;
    private String observacion;
}
