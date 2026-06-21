package com.rhu.api_platform.empleado;

import com.rhu.api_platform.empleado.dto.*;
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
@RequestMapping("/api/empleados")
@RequiredArgsConstructor
@Tag(name = "Empleados", description = "CRUD de empleados (5.2)")
public class EmpleadoController {

    private final EmpleadoService empleadoService;

    @PostMapping
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Crear empleado")
    public ResponseEntity<EmpleadoResponse> crear(@Valid @RequestBody CrearEmpleadoRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(empleadoService.crear(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Listar empleados con búsqueda y filtros")
    public List<EmpleadoResponse> listar(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) String estado,
            @RequestParam(required = false) String departamento) {
        return empleadoService.listar(q, estado, departamento);
    }

    @GetMapping("/contadores")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Contadores: total, activos, inactivos")
    public ContadoresResponse contadores() {
        return empleadoService.obtenerContadores();
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Obtener empleado por ID")
    public EmpleadoResponse obtener(@PathVariable Long id) {
        return empleadoService.obtener(id);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Actualizar empleado")
    public EmpleadoResponse actualizar(@PathVariable Long id, @Valid @RequestBody CrearEmpleadoRequest req) {
        return empleadoService.actualizar(id, req);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Cambiar estado del empleado")
    public EmpleadoResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambioEstadoRequest req) {
        return empleadoService.cambiarEstado(id, req.getEstado());
    }
}
