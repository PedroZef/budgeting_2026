package br.com.budgeting.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "API de Orçamento Inteligente (IA)",
        version = "v1",
        description = "API que utiliza Inteligência Artificial para processar comandos de voz e gerenciar transações financeiras."
    )
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    description = "Autenticação baseada em JWT. Use o endpoint `/api/auth/login` para gerar o token."
)
public class OpenApiConfig {
}