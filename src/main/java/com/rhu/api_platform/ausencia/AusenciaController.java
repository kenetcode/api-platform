package com.rhu.api_platform.ausencia;

import com.rhu.api_platform.ausencia.dto.AusenciaRequest;
import com.rhu.api_platform.ausencia.dto.AusenciaResponse;
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
@Tag(name = "Ausencias e Incapacidades", description = "Módulo 5.4 — Control unificado de ausencias e incapacidades")
public class AusenciaController {

    private final AusenciaService ausenciaService;

    @PostMapping("/api/empleados/{id}/ausencias")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR')")
    @Operation(summary = "Registrar ausencia o incapacidad")
    public ResponseEntity<AusenciaResponse> crear(@PathVariable Long id,
                                                  @Valid @RequestBody AusenciaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ausenciaService.crear(id, req));
    }

    @GetMapping("/api/empleados/{id}/ausencias")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Listar ausencias e incapacidades del empleado")
    public List<AusenciaResponse> listarPorEmpleado(@PathVariable Long id) {
        return ausenciaService.listarPorEmpleado(id);
    }

    @GetMapping("/api/empleados/{id}/incapacidades")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Listar incapacidades del empleado (filtra ausencias de tipo incapacidad)")
    public List<AusenciaResponse> listarIncapacidadesPorEmpleado(@PathVariable Long id) {
        return ausenciaService.listarIncapacidadesPorEmpleado(id);
    }

    @PutMapping("/api/empleados/{id}/ausencias/{ausenciaId}")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR')")
    @Operation(summary = "Actualizar ausencia o incapacidad")
    public AusenciaResponse actualizar(@PathVariable Long id,
                                       @PathVariable Long ausenciaId,
                                       @Valid @RequestBody AusenciaRequest req) {
        return ausenciaService.actualizar(id, ausenciaId, req);
    }

    @DeleteMapping("/api/empleados/{id}/ausencias/{ausenciaId}")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Eliminar ausencia o incapacidad")
    public ResponseEntity<Void> eliminar(@PathVariable Long id, @PathVariable Long ausenciaId) {
        ausenciaService.eliminar(id, ausenciaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/ausencias")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Listar ausencias e incapacidades por período (YYYY-MM)")
    public List<AusenciaResponse> listarPorPeriodo(@RequestParam String periodo) {
        return ausenciaService.listarPorPeriodo(periodo);
    }
}
