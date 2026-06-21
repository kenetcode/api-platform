package com.rhu.api_platform.common.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class RespuestaError {
    private String codigo;
    private String mensaje;
    private List<DetalleError> detalles;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class DetalleError {
        private String campo;
        private String error;
    }
}
