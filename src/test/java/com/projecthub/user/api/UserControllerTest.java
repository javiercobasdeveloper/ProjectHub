package com.projecthub.user.api;

// Importa componentes de Spring Boot para probar el registro con persistencia real de prueba.
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import com.projecthub.user.persistence.UserRepository;
import com.projecthub.project.persistence.ProjectRepository;
import com.projecthub.task.persistence.TaskRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Prueba el alta pública con Spring MVC, BCrypt, JPA y las migraciones Flyway.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class UserControllerTest {

    // Cliente HTTP de pruebas para ejecutar la ruta real de registro.
    @Autowired
    private MockMvc mockMvc;

    // Repositorio para aislar cada test y comprobar que se guarda el hash.
    @Autowired
    private UserRepository userRepository;

    // Repositorio para limpiar proyectos que todavía puedan referenciar usuarios.
    @Autowired
    private ProjectRepository projectRepository;

    // Repositorio para respetar primero la relación tarea-proyecto durante la limpieza.
    @Autowired
    private TaskRepository taskRepository;

    // Codificador real para verificar que la contraseña no se persiste en texto claro.
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Elimina las cuentas de pruebas antes de cada escenario de registro.
    @BeforeEach
    void clearUsers() {
        // Elimina primero las tareas que dependen de proyectos.
        taskRepository.deleteAll();
        // Elimina proyectos antes que los usuarios propietarios.
        projectRepository.deleteAll();
        // Limpia la tabla para que los escenarios no dependan del orden de ejecución.
        userRepository.deleteAll();
    }

    // Comprueba que el registro crea un MEMBER sin devolver la contraseña ni su hash.
    @Test
    void registersMemberWithEncodedPassword() throws Exception {
        // Envía el registro y comprueba el estado y la ubicación del recurso creado.
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"New.User@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.email").value("new.user@example.com"))
                .andExpect(jsonPath("$.globalRole").value("MEMBER"))
                .andExpect(jsonPath("$.password").doesNotExist())
                .andExpect(jsonPath("$.passwordHash").doesNotExist());
        // Verifica que el correo se normaliza y que BCrypt puede validar la contraseña.
        org.assertj.core.api.Assertions.assertThat(userRepository.findByEmailIgnoreCase("new.user@example.com"))
                .hasValueSatisfying(user -> {
                    org.assertj.core.api.Assertions.assertThat(user.getPasswordHash()).isNotEqualTo("password123");
                    org.assertj.core.api.Assertions.assertThat(passwordEncoder.matches("password123", user.getPasswordHash()))
                            .isTrue();
                });
    }

    // Comprueba que una dirección de correo no puede registrarse dos veces.
    @Test
    void rejectsDuplicateEmail() throws Exception {
        // Registra una cuenta válida para reservar el correo.
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"person@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isCreated());
        // Intenta registrar de nuevo el mismo correo con distinto uso de mayúsculas.
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"PERSON@example.com\",\"password\":\"password123\"}"))
                .andExpect(status().isConflict());
    }

    // Comprueba que las credenciales no cumplen si la contraseña es demasiado corta.
    @Test
    void rejectsShortPassword() throws Exception {
        // Envía una contraseña menor al mínimo requerido y espera HTTP 400.
        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"email\":\"person@example.com\",\"password\":\"short\"}"))
                .andExpect(status().isBadRequest());
    }
}