package com.rhu.api_platform.empresa.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class EmpresaResponse {
    private Long id;
    private String nombre;
    private String logoUrl;
    private String direccion;
    private String nit;
    private MenuInfo menu;

    @Data
    @Builder
    public static class MenuInfo {
        private String titulo;
        private String version;
        private String[] modulos;
    }
}
