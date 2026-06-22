package com.rhu.api_platform.config;

import com.rhu.api_platform.empresa.ParametroEmpresaRepository;
import com.rhu.api_platform.empresa.entity.ParametroEmpresa;
import com.rhu.api_platform.empresa.entity.TipoParametroEmpresa;
import com.rhu.api_platform.security.ApiKeyFiltro;
import com.rhu.api_platform.security.RolUsuario;
import com.rhu.api_platform.usuario.UsuarioRepository;
import com.rhu.api_platform.usuario.entity.Usuario;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.Month;
import java.util.UUID;

/**
 * Crea el usuario RRHH administrador por defecto al iniciar la aplicación
 * si no existe ningún usuario en la base de datos.
 *
 * Credenciales por defecto:
 *   Correo    : admin@lacesta.com
 *   Contraseña: Admin1234!
 *
 * El flujo de login es:
 *   POST /api/auth/login  →  { apiKey, nombre, rol }
 *   La UI usa esa apiKey en X-API-Key para las siguientes peticiones.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class InicializadorDatos implements CommandLineRunner {

    private static final String PASSWORD_DEFAULT = "Admin1234!";

    private final UsuarioRepository usuarioRepository;
    private final ParametroEmpresaRepository parametroEmpresaRepository;

    @Override
    public void run(String... args) {
        inicializarParametrosEmpresa();

        if (usuarioRepository.count() == 0) {
            // API key inicial (se regenera en cada login, esta es solo para el primer arranque)
            String apiKeyPlana = UUID.randomUUID().toString().replace("-", "")
                    + UUID.randomUUID().toString().replace("-", "");

            Usuario admin = Usuario.builder()
                    .nombre("Administrador")
                    .username("admin")
                    .correo("admin@lacesta.com")
                    .rol(RolUsuario.RRHH)
                    .apiKeyHash(ApiKeyFiltro.hashApiKey(apiKeyPlana))
                    .passwordHash(ApiKeyFiltro.hashApiKey(PASSWORD_DEFAULT))
                    .activo(true)
                    .build();
            usuarioRepository.save(admin);

            log.warn("=================================================================");
            log.warn("  USUARIO ADMIN CREADO AUTOMÁTICAMENTE");
            log.warn("  Usuario   : admin");
            log.warn("  Correo    : admin@lacesta.com");
            log.warn("  Contraseña: {}", PASSWORD_DEFAULT);
            log.warn("  Rol       : RRHH");
            log.warn("  Login     : POST /api/auth/login");
            log.warn("  Cambia la contraseña tras el primer login.");
            log.warn("=================================================================");
        }
    }

    private void inicializarParametrosEmpresa() {
        int anioActual = LocalDate.now().getYear();

        crearParametroSiNoExiste(
                "EMPRESA_ASUME_3_DIAS_INCAPACIDAD",
                "true",
                TipoParametroEmpresa.BOOLEAN,
                "La empresa asume el pago de los primeros 3 días de incapacidad común.");

        crearParametroSiNoExiste(
                "EMPRESA_PORCENTAJE_PAGO_3_DIAS",
                "100.00",
                TipoParametroEmpresa.DECIMAL,
                "Porcentaje de pago de los primeros 3 días de incapacidad común (cuando aplica).");

        crearParametroSiNoExiste(
                "EMPRESA_FECHA_PAGO_AGUINALDO",
                LocalDate.of(anioActual, Month.DECEMBER, 20).toString(),
                TipoParametroEmpresa.DATE,
                "Fecha de pago del aguinaldo dentro de la ventana legal (20 oct - 20 dic).");
    }

    private void crearParametroSiNoExiste(String clave, String valor, TipoParametroEmpresa tipo, String descripcion) {
        if (parametroEmpresaRepository.findByClave(clave).isEmpty()) {
            parametroEmpresaRepository.save(ParametroEmpresa.builder()
                    .clave(clave)
                    .valor(valor)
                    .tipo(tipo)
                    .descripcion(descripcion)
                    .build());
        }
    }
}
