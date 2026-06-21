package com.rhu.api_platform.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.Components;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("API Planillas - Supermercado La Cesta")
                .description("API REST para gestión de planillas. Autenticación por header X-API-Key.")
                .version("1.0.0"))
            .addSecurityItem(new SecurityRequirement().addList("ApiKey"))
            .components(new Components()
                .addSecuritySchemes("ApiKey", new SecurityScheme()
                    .name("X-API-Key")
                    .type(SecurityScheme.Type.APIKEY)
                    .in(SecurityScheme.In.HEADER)));
    }
}
