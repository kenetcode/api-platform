package com.rhu.api_platform.usuario.dto;

import com.rhu.api_platform.security.RolUsuario;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CrearUsuarioRequest {
    @NotBlank(message = "El nombre es obligatorio")
    private String nombre;

    @NotBlank(message = "El usuario es obligatorio")
    private String username;

    @NotBlank(message = "El correo es obligatorio")
    @Email(message = "Formato de correo inválido")
    private String correo;

    @NotNull(message = "El rol es obligatorio")
    private RolUsuario rol;

    // Contraseña inicial opcional. Si no se envía, se usa el correo como contraseña temporal.
    private String password;
}
