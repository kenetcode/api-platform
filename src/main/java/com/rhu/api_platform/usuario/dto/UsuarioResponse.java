package com.rhu.api_platform.usuario.dto;

import com.rhu.api_platform.security.RolUsuario;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class UsuarioResponse {
    private Long id;
    private String nombre;
    private String username;
    private String correo;
    private RolUsuario rol;
    private Boolean activo;
    private LocalDateTime creadoEn;
    // Solo se incluye al regenerar la key
    private String apiKeyPlana;
    // Solo se incluye al crear un usuario nuevo (contraseña inicial)
    private String passwordInicial;
}
