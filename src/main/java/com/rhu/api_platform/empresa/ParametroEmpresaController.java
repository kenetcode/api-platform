package com.rhu.api_platform.empresa;

import com.rhu.api_platform.empresa.dto.ParametroEmpresaRequest;
import com.rhu.api_platform.empresa.dto.ParametroEmpresaResponse;
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
@RequestMapping("/api/parametros-empresa")
@RequiredArgsConstructor
@Tag(name = "Parámetros de Empresa", description = "Configuración de parámetros operativos de La Cesta")
public class ParametroEmpresaController {

    private final ParametroEmpresaService parametroEmpresaService;

    @PostMapping
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Crear o actualizar un parámetro de empresa")
    public ResponseEntity<ParametroEmpresaResponse> guardar(@Valid @RequestBody ParametroEmpresaRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(parametroEmpresaService.crearOActualizar(req));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Listar parámetros de empresa")
    public List<ParametroEmpresaResponse> listar() {
        return parametroEmpresaService.listar();
    }

    @GetMapping("/{clave}")
    @PreAuthorize("hasAnyRole('RRHH','GERENCIA')")
    @Operation(summary = "Obtener parámetro por clave")
    public ParametroEmpresaResponse obtener(@PathVariable String clave) {
        return parametroEmpresaService.obtenerPorClave(clave);
    }
}
