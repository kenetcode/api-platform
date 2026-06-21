package com.rhu.api_platform.planilla.boleta;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Tag(name = "Boletas", description = "Generación de boletas JSON y PDF")
public class BoletaController {

    private final BoletaService boletaService;

    @GetMapping("/api/planillas/{planillaId}/empleados/{empleadoId}/boleta")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Boleta de pago en JSON")
    public BoletaResponse boletaJson(@PathVariable Long planillaId, @PathVariable Long empleadoId) {
        return boletaService.obtenerBoletaJson(planillaId, empleadoId);
    }

    @GetMapping("/api/planillas/{planillaId}/empleados/{empleadoId}/boleta/pdf")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Boleta de pago en PDF descargable")
    public ResponseEntity<byte[]> boletaPdf(@PathVariable Long planillaId, @PathVariable Long empleadoId) {
        byte[] pdf = boletaService.generarBoletaPdf(planillaId, empleadoId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"boleta_" + planillaId + "_" + empleadoId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/api/planillas/{planillaId}/exportar/pdf")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Exportar planilla completa en PDF")
    public ResponseEntity<byte[]> exportarPlanilla(@PathVariable Long planillaId) {
        byte[] pdf = boletaService.exportarPlanillaPdf(planillaId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"planilla_" + planillaId + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }
}
