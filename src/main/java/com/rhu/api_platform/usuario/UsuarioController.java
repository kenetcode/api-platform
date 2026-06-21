package com.rhu.api_platform.usuario;

import com.rhu.api_platform.usuario.dto.CambiarPasswordRequest;
import com.rhu.api_platform.usuario.dto.CrearUsuarioRequest;
import com.rhu.api_platform.usuario.dto.UsuarioResponse;
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
@RequestMapping("/api/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Gestión de usuarios del sistema")
public class UsuarioController {

    private final UsuarioService usuarioService;

    @PostMapping
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Crear usuario", description = "Devuelve la API key una sola vez en texto plano.")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest req) {
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.crearUsuario(req));
    }

    @GetMapping
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Listar usuarios")
    public List<UsuarioResponse> listar() {
        return usuarioService.listarUsuarios();
    }

    @PatchMapping("/{id}/activar")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Activar usuario")
    public UsuarioResponse activar(@PathVariable Long id) {
        return usuarioService.cambiarEstado(id, true);
    }

    @PatchMapping("/{id}/inactivar")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Inactivar usuario")
    public UsuarioResponse inactivar(@PathVariable Long id) {
        return usuarioService.cambiarEstado(id, false);
    }

    @PostMapping("/{id}/regenerar-api-key")
    @PreAuthorize("hasRole('RRHH')")
    @Operation(summary = "Regenerar API key", description = "Invalida la key anterior y devuelve una nueva una sola vez.")
    public UsuarioResponse regenerarApiKey(@PathVariable Long id) {
        return usuarioService.regenerarApiKey(id);
    }

    @PatchMapping("/{id}/cambiar-password")
    @PreAuthorize("hasAnyRole('RRHH','AUXILIAR','GERENCIA')")
    @Operation(summary = "Cambiar contraseña", description = "Cualquier usuario puede cambiar su propia contraseña.")
    public ResponseEntity<Void> cambiarPassword(@PathVariable Long id,
                                                @Valid @RequestBody CambiarPasswordRequest req) {
        usuarioService.cambiarPassword(id, req);
        return ResponseEntity.noContent().build();
    }
}
