package com.rhu.api_platform.empresa;

import com.rhu.api_platform.empresa.dto.DashboardResponse;
import com.rhu.api_platform.empresa.dto.EmpresaResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "Empresa", description = "Información de branding y dashboard")
public class EmpresaController {

    private final EmpresaService empresaService;

    @GetMapping("/empresa")
    @Operation(summary = "Datos de la empresa y menú")
    public EmpresaResponse obtenerEmpresa() {
        return empresaService.obtenerEmpresa();
    }

    @GetMapping("/dashboard")
    @Operation(summary = "Indicadores del tablero")
    public DashboardResponse dashboard() {
        return empresaService.obtenerDashboard();
    }
}
