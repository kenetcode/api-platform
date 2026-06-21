package com.rhu.api_platform.auth.dto;

import com.rhu.api_platform.security.RolUsuario;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private Long id;
    private String nombre;
    private String correo;
    private RolUsuario rol;
    private String apiKey;
}
