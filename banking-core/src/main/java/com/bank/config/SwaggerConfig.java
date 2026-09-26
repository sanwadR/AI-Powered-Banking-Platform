package com.bank.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * SwaggerConfig — configures the interactive API documentation.
 *
 * After starting the app, visit: http://localhost:8080/swagger-ui.html
 * You'll see all your endpoints, be able to test them, and see example requests/responses.
 *
 * @SecurityScheme tells Swagger: "There's a Bearer token auth scheme.
 *   Show a lock icon on protected endpoints and let users input their JWT token."
 */
@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "AI-Driven Banking Platform API",
        version = "1.0",
        description = "Production-grade banking backend with fraud detection and AI investigation"
    )
)
@SecurityScheme(
    name = "bearerAuth",
    description = "Paste your JWT token here (without 'Bearer ' prefix)",
    scheme = "bearer",
    type = SecuritySchemeType.HTTP,
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {
    // No code needed — annotations do all the configuration
}
