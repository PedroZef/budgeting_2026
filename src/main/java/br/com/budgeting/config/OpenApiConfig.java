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
    ),
    security = @SecurityRequirement(name = "basicAuth") // Aplica a segurança em todos os endpoints
)
@SecurityScheme(
    name = "basicAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "basic",
    description = "Autenticação básica. Use o usuário e senha configurados no Spring Security."
)
public class OpenApiConfig {
}