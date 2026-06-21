package com.rhu.api_platform.empleado.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class ContadoresResponse {
    private long total;
    private long activos;
    private long inactivos;
}
