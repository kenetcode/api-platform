package com.rhu.api_platform.auth;

import com.rhu.api_platform.auth.dto.LoginRequest;
import com.rhu.api_platform.auth.dto.LoginResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "Login con correo y contraseña")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Recibe correo y contraseña. Devuelve la apiKey que el frontend debe usar en el header X-API-Key para todas las demás peticiones."
    )
    public LoginResponse login(@Valid @RequestBody LoginRequest req) {
        return authService.login(req);
    }
}
