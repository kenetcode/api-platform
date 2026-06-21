package com.rhu.api_platform.usuario;

import com.rhu.api_platform.common.exception.ConflictoException;
import com.rhu.api_platform.common.exception.RecursoNoEncontradoException;
import com.rhu.api_platform.common.exception.ValidacionNegocioException;
import com.rhu.api_platform.security.ApiKeyFiltro;
import com.rhu.api_platform.usuario.dto.CambiarPasswordRequest;
import com.rhu.api_platform.usuario.dto.CrearUsuarioRequest;
import com.rhu.api_platform.usuario.dto.UsuarioResponse;
import com.rhu.api_platform.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;

    @Transactional
    public UsuarioResponse crearUsuario(CrearUsuarioRequest req) {
        if (usuarioRepository.existsByCorreo(req.getCorreo())) {
            throw new ConflictoException("Ya existe un usuario con el correo: " + req.getCorreo());
        }

        // Contraseña inicial: si no se especifica, se usa el correo como contraseña temporal
        String passwordInicial = (req.getPassword() != null && !req.getPassword().isBlank())
                ? req.getPassword() : req.getCorreo();

        String apiKeyPlana = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");

        Usuario usuario = Usuario.builder()
                .nombre(req.getNombre())
                .correo(req.getCorreo())
                .rol(req.getRol())
                .apiKeyHash(ApiKeyFiltro.hashApiKey(apiKeyPlana))
                .passwordHash(ApiKeyFiltro.hashApiKey(passwordInicial))
                .activo(true)
                .build();
        usuarioRepository.save(usuario);

        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .creadoEn(usuario.getCreadoEn())
                .passwordInicial(passwordInicial)
                .build();
    }

    public List<UsuarioResponse> listarUsuarios() {
        return usuarioRepository.findAll().stream()
                .map(u -> UsuarioResponse.builder()
                        .id(u.getId())
                        .nombre(u.getNombre())
                        .correo(u.getCorreo())
                        .rol(u.getRol())
                        .activo(u.getActivo())
                        .creadoEn(u.getCreadoEn())
                        .build())
                .toList();
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        usuario.setActivo(activo);
        usuarioRepository.save(usuario);
        return toResponse(usuario);
    }

    @Transactional
    public UsuarioResponse regenerarApiKey(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        String apiKeyPlana = UUID.randomUUID().toString().replace("-", "")
                + UUID.randomUUID().toString().replace("-", "");
        usuario.setApiKeyHash(ApiKeyFiltro.hashApiKey(apiKeyPlana));
        usuarioRepository.save(usuario);
        return UsuarioResponse.builder()
                .id(usuario.getId())
                .nombre(usuario.getNombre())
                .correo(usuario.getCorreo())
                .rol(usuario.getRol())
                .activo(usuario.getActivo())
                .creadoEn(usuario.getCreadoEn())
                .apiKeyPlana(apiKeyPlana)
                .build();
    }

    @Transactional
    public void cambiarPassword(Long id, CambiarPasswordRequest req) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("Usuario no encontrado: " + id));
        String hashActual = ApiKeyFiltro.hashApiKey(req.getPasswordActual());
        if (!hashActual.equals(usuario.getPasswordHash())) {
            throw new ValidacionNegocioException("La contraseña actual es incorrecta.");
        }
        usuario.setPasswordHash(ApiKeyFiltro.hashApiKey(req.getPasswordNueva()));
        usuarioRepository.save(usuario);
    }

    private UsuarioResponse toResponse(Usuario u) {
        return UsuarioResponse.builder()
                .id(u.getId())
                .nombre(u.getNombre())
                .correo(u.getCorreo())
                .rol(u.getRol())
                .activo(u.getActivo())
                .creadoEn(u.getCreadoEn())
                .build();
    }
}
