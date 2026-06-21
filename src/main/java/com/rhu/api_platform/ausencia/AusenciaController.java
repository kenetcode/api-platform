package com.rhu.api_platform.ausencia;

import com.rhu.api_platform.ausencia.dto.*;
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
@Tag(name = "Ausencias e Incapacidades", description = "Módulo 5.4 — Control de ausencias e incapacidades")
public class AusenciaController {

    private final AusenciaService ausenciaService;

    // ========== AUSENCIAS ==========

    @PostMapping("/api/empleados/{id}/ausencias")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR')")
    @Operation(summary = "Registrar ausencia")
    public ResponseEntity<AusenciaResponse> crearAusencia(@PathVariable Long id,
                                                          @Valid @RequestBody AusenciaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ausenciaService.crearAusencia(id, req));
    }

    @GetMapping("/api/empleados/{id}/ausencias")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Listar ausencias del empleado")
    public List<AusenciaResponse> listarAusencias(@PathVariable Long id) {
        return ausenciaService.listarAusencias(id);
    }

    @PutMapping("/api/empleados/{id}/ausencias/{ausenciaId}")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR')")
    @Operation(summary = "Actualizar ausencia")
    public AusenciaResponse actualizarAusencia(@PathVariable Long id,
                                               @PathVariable Long ausenciaId,
                                               @Valid @RequestBody AusenciaRequest req) {
        return ausenciaService.actualizarAusencia(id, ausenciaId, req);
    }

    @DeleteMapping("/api/empleados/{id}/ausencias/{ausenciaId}")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Eliminar ausencia")
    public ResponseEntity<Void> eliminarAusencia(@PathVariable Long id, @PathVariable Long ausenciaId) {
        ausenciaService.eliminarAusencia(id, ausenciaId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/api/ausencias")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Listar ausencias por período (YYYY-MM)")
    public List<AusenciaResponse> listarPorPeriodo(@RequestParam String periodo) {
        return ausenciaService.listarPorPeriodo(periodo);
    }

    // ========== INCAPACIDADES ==========

    @PostMapping("/api/empleados/{id}/incapacidades")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR')")
    @Operation(summary = "Registrar incapacidad")
    public ResponseEntity<IncapacidadResponse> crearIncapacidad(@PathVariable Long id,
                                                                @Valid @RequestBody IncapacidadRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ausenciaService.crearIncapacidad(id, req));
    }

    @GetMapping("/api/empleados/{id}/incapacidades")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Listar incapacidades del empleado")
    public List<IncapacidadResponse> listarIncapacidades(@PathVariable Long id) {
        return ausenciaService.listarIncapacidades(id);
    }

    @PutMapping("/api/empleados/{id}/incapacidades/{incId}")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR')")
    @Operation(summary = "Actualizar incapacidad")
    public IncapacidadResponse actualizarIncapacidad(@PathVariable Long id,
                                                     @PathVariable Long incId,
                                                     @Valid @RequestBody IncapacidadRequest req) {
        return ausenciaService.actualizarIncapacidad(id, incId, req);
    }

    @DeleteMapping("/api/empleados/{id}/incapacidades/{incId}")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Eliminar incapacidad")
    public ResponseEntity<Void> eliminarIncapacidad(@PathVariable Long id, @PathVariable Long incId) {
        ausenciaService.eliminarIncapacidad(id, incId);
        return ResponseEntity.noContent().build();
    }
}
