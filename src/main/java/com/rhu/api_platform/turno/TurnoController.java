package com.rhu.api_platform.turno;

import com.rhu.api_platform.turno.dto.TurnoRequest;
import com.rhu.api_platform.turno.dto.TurnoResponse;
import com.rhu.api_platform.turno.entity.EstadoTurno;
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
@RequestMapping("/api/turnos")
@RequiredArgsConstructor
@Tag(name = "Turnos", description = "Gestión de turnos y horarios laborales")
public class TurnoController {

    private final TurnoService turnoService;

    @PostMapping
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Crear turno laboral")
    public ResponseEntity<TurnoResponse> crear(@Valid @RequestBody TurnoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(turnoService.crear(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Listar turnos")
    public List<TurnoResponse> listar() {
        return turnoService.listar();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Obtener turno por ID")
    public TurnoResponse obtener(@PathVariable Long id) {
        return turnoService.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Actualizar turno")
    public TurnoResponse actualizar(@PathVariable Long id, @Valid @RequestBody TurnoRequest req) {
        return turnoService.actualizar(id, req);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Activar o inactivar turno")
    public TurnoResponse cambiarEstado(@PathVariable Long id, @RequestParam EstadoTurno estado) {
        return turnoService.cambiarEstado(id, estado);
    }
}
