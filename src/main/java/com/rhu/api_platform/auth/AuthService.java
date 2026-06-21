package com.rhu.api_platform.auth;

import com.rhu.api_platform.auth.dto.LoginRequest;
import com.rhu.api_platform.auth.dto.LoginResponse;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.security.ApiKeyFiltro;
import com.rhu.api_platform.usuario.UsuarioRepository;
import com.rhu.api_platform.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UsuarioRepository usuarioRepository;

    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByCorreo(req.getCorreo())
                .orElseThrow(() -> new ValidacionNegocioException("Correo o contraseña incorrectos."));

        if (!usuario.getActivo()) {
            throw new ValidacionNegocioException("El usuario está inactivo.");
        }

        String hashIngresado = ApiKeyFiltro.hashApiKey(req.getPassword());
        if (!hashIngresado.equals(usuario.getPasswordHash())) {
            throw new ValidacionNegocioException("Correo o contraseña incorrectos.");
        }

        // Devuelve la apiKey en texto plano para que el frontend la use en headers
        // La apiKey plana no se almacena — se reconstruye aquí desde el hash no,
        // así que regeneramos una nueva si no se guardó nunca la plana.
        // En su lugar, guardamos la apiKey plana encriptada con el mismo mecanismo.
        // NOTA: la apiKey se guarda hasheada por seguridad, no podemos "recuperarla".
        // El login devuelve la apiKey plana solo si fue guardada en la sesión.
        // Solución: guardamos también la apiKey plana cifrada (reversible con salt).
        // Implementación simple: la apiKey plana se guarda en un campo separado
        // o usamos la contraseña como fuente de verdad para autenticar y luego
        // la UI usa la apiKey que ya tiene guardada.
        //
        // Solución práctica adoptada: al hacer login exitoso, regeneramos la apiKey
        // y la devolvemos. El usuario siempre obtiene una apiKey fresca al logear.
        // Esto es seguro: si pierden acceso, el próximo login genera una nueva.
        String apiKeyPlana = java.util.UUID.randomUUID().toString().replace("-", "")
                + java.util.UUID.randomUUID().toString().replace("-", "");
        String nuevoHash = ApiKeyFiltro.hashApiKey(apiKeyPlana);
        usuario.setApiKeyHash(nuevoHash);
        usuarioRepository.save(usuario);

        return LoginResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .apiKey(apiKeyPlana)
                .build();
    }
}
