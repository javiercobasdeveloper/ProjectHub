package com.projecthub.config;

// Importa dependencias de Spring para probar la documentación generada mediante HTTP.
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Comprueba el contrato OpenAPI y que su interfaz se sirve sin debilitar la API protegida.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class OpenApiControllerTest {

    // Cliente HTTP de pruebas para consultar Swagger UI y la especificación JSON.
    @Autowired
    private MockMvc mockMvc;

    // Verifica que el contrato es público y declara correctamente HTTP Basic.
    @Test
    void publishesOpenApiSpecificationWithAuthenticationRequirements() throws Exception {
        // Descarga el documento generado sin proporcionar credenciales.
        mockMvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.info.title").value("ProjectHub API"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.type").value("http"))
                .andExpect(jsonPath("$.components.securitySchemes.basicAuth.scheme").value("basic"))
                .andExpect(jsonPath("$.paths['/api/projects'].post.responses['201'].description").value("Proyecto creado"))
                .andExpect(jsonPath("$.paths['/api/projects'].get.security[0].basicAuth").exists())
                .andExpect(jsonPath("$.paths['/api/projects'].post.security[0].basicAuth").exists())
                .andExpect(jsonPath("$.paths['/api/projects/{id}'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/tasks/{id}'].patch").exists())
                .andExpect(jsonPath("$.paths['/api/tasks/{id}'].delete.responses['204']").exists())
                .andExpect(jsonPath("$.components.schemas.TaskResponse.properties.priority").exists())
                .andExpect(jsonPath("$.components.schemas.TaskResponse.properties.assigneeId").exists())
                .andExpect(jsonPath("$.paths['/api/users'].post.security").doesNotExist());
    }

    // Verifica que Swagger UI carga de forma pública para poder explorar el contrato.
    @Test
    void servesSwaggerUiWithoutAuthentication() throws Exception {
        // Solicita el recurso estático principal de Swagger UI sin autenticación.
        mockMvc.perform(get("/swagger-ui/index.html"))
                .andExpect(status().isOk());
    }
}