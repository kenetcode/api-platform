package com.rhu.api_platform.usuario;

import com.rhu.api_platform.usuario.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {
    Optional<Usuario> findByApiKeyHashAndActivoTrue(String apiKeyHash);
    Optional<Usuario> findByCorreo(String correo);
    Optional<Usuario> findByUsername(String username);
    boolean existsByCorreo(String correo);
    boolean existsByUsername(String username);
}
