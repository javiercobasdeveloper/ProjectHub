package com.projecthub.config;

// Importa anotaciones OpenAPI para describir información general y HTTP Basic.
import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

// Define metadatos compartidos que springdoc utiliza para generar la especificación.
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "ProjectHub API",
        version = "1.0.0",
        description = "API REST para gestionar proyectos y tareas. Las operaciones de proyectos y tareas requieren HTTP Basic y solo permiten acceder a recursos propios."
))
@SecurityScheme(
        name = "basicAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "basic",
        description = "Correo y contraseña de una cuenta registrada en ProjectHub."
)
public class OpenApiConfig {
}