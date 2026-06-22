package com.rhu.api_platform.empresa.dto;

import com.rhu.api_platform.empresa.entity.TipoParametroEmpresa;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ParametroEmpresaRequest {

    @NotBlank(message = "La clave es obligatoria")
    private String clave;

    @NotBlank(message = "El valor es obligatorio")
    private String valor;

    @NotNull(message = "El tipo es obligatorio")
    private TipoParametroEmpresa tipo;

    private String descripcion;

    private LocalDate vigencia;
}
