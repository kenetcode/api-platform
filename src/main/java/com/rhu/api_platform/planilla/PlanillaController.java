package com.rhu.api_platform.planilla;

import com.rhu.api_platform.planilla.dto.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Tag(name = "Planillas", description = "Motor de cálculo y gestión de planillas (5.3)")
public class PlanillaController {

    private final PlanillaService planillaService;
    private final ParametroLegalService parametroLegalService;

    @PostMapping("/api/planillas")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Crear período de planilla")
    public ResponseEntity<PlanillaResponse> crear(@Valid @RequestBody CrearPlanillaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(planillaService.crearPlanilla(req));
    }

    @PostMapping("/api/planillas/{id}/detalles")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR')")
    @Operation(summary = "Capturar datos de un empleado en la planilla")
    public DetallePlanillaResponse capturarDetalle(@PathVariable Long id,
                                                   @Valid @RequestBody CapturarDetalleRequest req) {
        return planillaService.capturarDetalle(id, req);
    }

    @PostMapping("/api/planillas/{id}/calcular")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Ejecutar el motor de cálculo sobre todos los detalles")
    public PlanillaResponse calcular(@PathVariable Long id) {
        return planillaService.calcular(id);
    }

    @GetMapping("/api/planillas/{id}")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Obtener planilla con detalles y totales")
    public PlanillaResponse obtener(@PathVariable Long id) {
        return planillaService.obtener(id);
    }

    @PostMapping("/api/planillas/{id}/aprobar")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Aprobar planilla calculada")
    public PlanillaResponse aprobar(@PathVariable Long id) {
        return planillaService.aprobar(id);
    }

    @GetMapping("/api/planillas")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Histórico de planillas")
    public List<PlanillaResponse> listar() {
        return planillaService.listar();
    }

    @GetMapping("/api/empleados/{id}/prestaciones")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Proyección de prestaciones: ?tipo=vacaciones|aguinaldo")
    public PrestacionesResponse prestaciones(@PathVariable Long id,
                                             @RequestParam String tipo) {
        return planillaService.calcularPrestaciones(id, tipo);
    }

    @GetMapping("/api/parametros-legales")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Listar parámetros legales configurables")
    public List<com.rhu.api_platform.planilla.entity.ParametroLegal> listarParametros() {
        return parametroLegalService.listar();
    }
}
