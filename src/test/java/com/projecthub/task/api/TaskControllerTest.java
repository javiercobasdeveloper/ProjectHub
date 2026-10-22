package com.projecthub.task.api;

// Importa el contexto completo para probar conjuntamente MVC, servicios y persistencia.
import org.springframework.boot.test.context.SpringBootTest;
// Importa MockMvc para ejecutar peticiones HTTP sin iniciar un servidor de red.
import org.springframework.test.web.servlet.MockMvc;
// Importa la configuración de pruebas MVC de Spring Boot 4.
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
// Importa la inyección de dependencias para usar el cliente y repositorios en los tests.
import org.springframework.beans.factory.annotation.Autowired;
// Importa el perfil que ejecuta las migraciones sobre una base H2 desechable.
import org.springframework.test.context.ActiveProfiles;
// Importa anotaciones para preparar el estado antes de cada caso de prueba.
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
// Importa los repositorios para limpiar las filas creadas por cada test.
import com.projecthub.project.persistence.ProjectRepository;
import com.projecthub.task.persistence.TaskRepository;
import com.projecthub.user.GlobalRole;
import com.projecthub.user.persistence.UserEntity;
import com.projecthub.user.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Ejecuta los casos de tareas contra el esquema real creado por Flyway en H2.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class TaskControllerTest {

    // Cliente HTTP de pruebas para llamar a los endpoints registrados.
    @Autowired
    private MockMvc mockMvc;

    // Repositorio de tareas para limpiar la base antes de cada caso.
    @Autowired
    private TaskRepository taskRepository;

    // Repositorio de proyectos, que deben conservarse solo si tienen tareas asociadas.
    @Autowired
    private ProjectRepository projectRepository;

        // Repositorio que prepara identidades para verificar propiedad de tareas.
        @Autowired
        private UserRepository userRepository;

        // Codificador que permite preparar credenciales válidas para HTTP Basic.
        @Autowired
        private PasswordEncoder passwordEncoder;

    // Elimina primero tareas y después proyectos para respetar la clave foránea.
    @BeforeEach
    void clearProjectsAndTasks() {
        // Borra las tareas dependientes antes de tocar sus proyectos propietarios.
        taskRepository.deleteAll();
        // Deja vacía la tabla de proyectos para aislar el siguiente escenario.
        projectRepository.deleteAll();
        // Elimina usuarios después de borrar los proyectos que pueden referenciarlos.
        userRepository.deleteAll();
        // Crea el propietario que usarán las peticiones autenticadas de esta prueba.
        userRepository.save(new UserEntity("owner@example.com", passwordEncoder.encode("password123"), GlobalRole.MEMBER));
    }

    // Comprueba creación, lectura, listado y transiciones válidas e inválidas de una tarea.
    @Test
    void createsReadsListsAndChangesTaskStatus() throws Exception {
        // Crea el proyecto padre para obtener el recurso anidado de tareas.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"ProjectHub\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Crea una tarea y conserva la URL específica devuelta para consultarla.
        String taskLocation = mockMvc.perform(post(projectLocation + "/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Diseñar API\",\"description\":\"Contrato inicial\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.projectId").isNumber())
                .andExpect(jsonPath("$.description").value("Contrato inicial"))
                .andExpect(jsonPath("$.priority").value("MEDIUM"))
                .andExpect(jsonPath("$.assigneeId").isNumber())
                .andExpect(jsonPath("$.status").value("TODO"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Impide completar directamente una tarea pendiente y conserva su estado TODO.
        mockMvc.perform(post(taskLocation + "/complete").with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isConflict());
        mockMvc.perform(get(taskLocation).with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"));

        // Comprueba el ciclo permitido TODO -> IN_PROGRESS -> TODO.
        mockMvc.perform(post(taskLocation + "/start").with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
        mockMvc.perform(post(taskLocation + "/return-to-todo").with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("TODO"));

        // Completa la tarea siguiendo el camino permitido desde IN_PROGRESS.
        mockMvc.perform(post(taskLocation + "/start").with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk());
        mockMvc.perform(post(taskLocation + "/complete").with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DONE"));

        // Comprueba que una tarea finalizada aparece en la lista de su proyecto.
        mockMvc.perform(get(projectLocation + "/tasks").with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Diseñar API"))
                .andExpect(jsonPath("$[0].description").value("Contrato inicial"))
                .andExpect(jsonPath("$[0].status").value("DONE"));
    }

    // Comprueba que un título compuesto por espacios no se persiste.
    @Test
    void rejectsBlankTaskTitle() throws Exception {
        // Crea el proyecto necesario para que el rechazo se deba al título de tarea.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"ProjectHub\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Envía un título sin contenido y verifica que Jakarta Validation devuelve HTTP 400.
        mockMvc.perform(post(projectLocation + "/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

    // Comprueba que una tarea no puede crearse bajo un proyecto inexistente.
    @Test
    void returnsNotFoundWhenParentProjectDoesNotExist() throws Exception {
        // Intenta crear una tarea bajo un identificador de proyecto que no existe.
        mockMvc.perform(post("/api/projects/999999/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Diseñar API\"}"))
                .andExpect(status().isNotFound());
    }

    // Comprueba que un proyecto archivado no admite tareas nuevas.
    @Test
    void rejectsTaskCreationWhenProjectIsArchived() throws Exception {
        // Crea un proyecto propio para archivarlo antes de probar la creación de una tarea.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Proyecto archivado\"}"))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Archiva el proyecto usando la cuenta propietaria.
        mockMvc.perform(post(projectLocation + "/archive")
                        .with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk());

        // Rechaza la creación para mantener inmutable el contenido del proyecto archivado.
        mockMvc.perform(post(projectLocation + "/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Tarea nueva\"}"))
                .andExpect(status().isConflict());
    }

    // Comprueba que la API rechaza una descripción que supera los 2000 caracteres.
    @Test
    void rejectsDescriptionLongerThanTwoThousandCharacters() throws Exception {
        // Crea un proyecto propio para que la petición alcance la validación de tarea.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"ProjectHub\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        // Genera el JSON con una descripción que supera el límite por un carácter.
        String longDescription = "a".repeat(2001);
        String requestBody = "{\"title\":\"Tarea válida\",\"description\":\"" + longDescription + "\"}";

        // La validación HTTP rechaza la petición antes de crear la tarea.
        mockMvc.perform(post(projectLocation + "/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isBadRequest());
    }

    // Comprueba que otra cuenta no puede consultar ni cambiar una tarea ajena.
    @Test
    void hidesAnotherUsersTask() throws Exception {
        // Crea un proyecto y una tarea bajo la cuenta propietaria.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Privado\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        String taskLocation = mockMvc.perform(post(projectLocation + "/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Tarea privada\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        // Prepara otra cuenta cuyo acceso no debe incluir la tarea ni su proyecto.
        userRepository.save(new UserEntity("other@example.com", passwordEncoder.encode("password123"), GlobalRole.MEMBER));

        // La lectura y la transición de una tarea ajena producen el mismo 404.
        mockMvc.perform(get(taskLocation).with(httpBasic("other@example.com", "password123")))
                .andExpect(status().isNotFound());
        mockMvc.perform(post(taskLocation + "/start").with(httpBasic("other@example.com", "password123")))
                .andExpect(status().isNotFound());
        // También bloquea modificaciones de los campos de una tarea ajena.
        mockMvc.perform(patch(taskLocation)
                        .with(httpBasic("other@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Modificada\"}"))
                .andExpect(status().isNotFound());
        // El usuario ajeno tampoco puede eliminar la tarea.
        mockMvc.perform(delete(taskLocation).with(httpBasic("other@example.com", "password123")))
                .andExpect(status().isNotFound());
    }

    // Comprueba que PATCH cambia campos propios y conserva estado y responsable.
    @Test
    void updatesOwnTaskFieldsWithoutChangingItsStatusOrAssignee() throws Exception {
        // Crea el proyecto para que la tarea herede un propietario y responsable.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"ProjectHub\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        // Crea una tarea con prioridad predeterminada.
        String taskLocation = mockMvc.perform(post(projectLocation + "/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Inicial\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Actualiza título, descripción y prioridad sin enviar estado ni responsable.
        mockMvc.perform(patch(taskLocation)
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Actualizada\",\"description\":\"Detalle\",\"priority\":\"HIGH\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Actualizada"))
                .andExpect(jsonPath("$.description").value("Detalle"))
                .andExpect(jsonPath("$.priority").value("HIGH"))
                .andExpect(jsonPath("$.status").value("TODO"))
                .andExpect(jsonPath("$.assigneeId").isNumber());

        // Una segunda actualización parcial cambia solo la descripción y prioridad.
        mockMvc.perform(patch(taskLocation)
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"description\":\"\",\"priority\":\"LOW\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Actualizada"))
                .andExpect(jsonPath("$.description").value(""))
                .andExpect(jsonPath("$.priority").value("LOW"));
    }

    // Comprueba que una prioridad que no pertenece al enum se rechaza por HTTP.
    @Test
    void rejectsUnknownPriority() throws Exception {
        // Crea un proyecto para que la validación del cuerpo sea el motivo del rechazo.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"ProjectHub\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Envía un valor de prioridad que no existe en TaskPriority.
        mockMvc.perform(post(projectLocation + "/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Tarea\",\"priority\":\"CRITICAL\"}"))
                .andExpect(status().isBadRequest());
    }

    // Comprueba que el propietario puede eliminar una tarea y que deja de ser consultable.
    @Test
    void deletesOwnTask() throws Exception {
        // Crea el proyecto que contendrá la tarea que se eliminará.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"ProjectHub\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        // Crea una tarea propia y conserva su URI.
        String taskLocation = mockMvc.perform(post(projectLocation + "/tasks")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"title\":\"Eliminar\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // El propietario elimina la tarea y recibe una respuesta sin cuerpo.
        mockMvc.perform(delete(taskLocation).with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isNoContent());
        // Comprueba que el recurso ya no existe después del borrado.
        mockMvc.perform(get(taskLocation).with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isNotFound());
    }
}