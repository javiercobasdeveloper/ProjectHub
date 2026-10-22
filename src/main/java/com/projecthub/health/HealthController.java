package com.projecthub.health;

// Importa metadatos para incluir el endpoint público en la documentación OpenAPI.
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
// Importa la anotación para declarar una clase que atiende peticiones HTTP y serializa sus respuestas.
import org.springframework.web.bind.annotation.GetMapping;
// Importa la anotación para registrar esta clase como controlador REST.
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

// Expone una comprobación sencilla para confirmar que el servidor web está disponible.
@RestController
@Tag(name = "Salud", description = "Disponibilidad del servicio")
public class HealthController {

    // Responde a GET /api/health con el estado básico del servicio.
    @GetMapping("/api/health")
    @Operation(summary = "Comprobar salud de la API")
    public Map<String, String> health() {
        // Devuelve un objeto simple que Spring convierte a JSON.
        return Map.of("status", "UP");
    }
}