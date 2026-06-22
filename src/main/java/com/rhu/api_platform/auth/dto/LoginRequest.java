package com.rhu.api_platform.auth.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class LoginRequest {
    @NotBlank(message = "El usuario o correo es obligatorio")
    private String usuario;

    @NotBlank(message = "La contraseña es obligatoria")
    private String password;
}
