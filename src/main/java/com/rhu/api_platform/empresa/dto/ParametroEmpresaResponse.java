package com.rhu.api_platform.empresa.dto;

import com.rhu.api_platform.empresa.entity.TipoParametroEmpresa;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Builder
public class ParametroEmpresaResponse {
    private Long id;
    private String clave;
    private String valor;
    private TipoParametroEmpresa tipo;
    private String descripcion;
    private LocalDate vigencia;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;
}
