package com.projecthub.health;

// Importa el arranque del contexto real de Spring para probar el endpoint registrado.
import org.springframework.boot.test.context.SpringBootTest;
// Importa MockMvc para enviar peticiones HTTP de prueba sin levantar un puerto de red.
import org.springframework.test.web.servlet.MockMvc;
// Importa la configuración automática de MockMvc en el contexto de pruebas.
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
// Importa la anotación que solicita a Spring inyectar una dependencia en el test.
import org.springframework.beans.factory.annotation.Autowired;
// Activa la configuración H2 aislada para que esta prueba no dependa de PostgreSQL local.
import org.springframework.test.context.ActiveProfiles;
// Importa la anotación que marca este método como una prueba de JUnit.
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Carga la aplicación y configura un cliente HTTP de prueba para verificar la ruta.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class HealthControllerTest {

    // Spring inyecta el cliente de pruebas que ejecuta peticiones contra el contexto cargado.
    @Autowired
    private MockMvc mockMvc;

    // Comprueba que la ruta de salud responde correctamente y entrega JSON con estado UP.
    @Test
    void returnsUpWhenApplicationIsRunning() throws Exception {
        // Envía GET /api/health y verifica tanto el código HTTP como el cuerpo JSON.
        mockMvc.perform(get("/api/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"));
    }
}