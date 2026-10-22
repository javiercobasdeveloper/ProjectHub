package com.projecthub.project.api;

// Importa la prueba con el contexto completo de Spring Boot.
import org.springframework.boot.test.context.SpringBootTest;
// Importa MockMvc para probar rutas HTTP sin arrancar un puerto de red.
import org.springframework.test.web.servlet.MockMvc;
// Importa la configuración de MockMvc de Spring Boot 4.
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
// Importa la inyección de dependencias para usar MockMvc y el repositorio en las pruebas.
import org.springframework.beans.factory.annotation.Autowired;
// Importa el perfil que configura H2 y ejecuta Flyway durante los tests.
import org.springframework.test.context.ActiveProfiles;
// Importa la anotación que prepara datos antes de cada prueba.
import org.junit.jupiter.api.BeforeEach;
// Importa la anotación que identifica cada prueba automática.
import org.junit.jupiter.api.Test;
// Importa el repositorio para limpiar los datos entre casos de prueba.
import com.projecthub.project.persistence.ProjectRepository;
import com.projecthub.user.GlobalRole;
import com.projecthub.user.persistence.UserEntity;
import com.projecthub.user.persistence.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// Ejecuta las peticiones contra Spring, JPA y la migración real usando una base H2 aislada.
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class ProjectControllerTest {

    // Cliente HTTP de pruebas que ejecuta las rutas registradas por Spring MVC.
    @Autowired
    private MockMvc mockMvc;

    // Repositorio usado para que cada prueba empiece sin proyectos previos.
    @Autowired
    private ProjectRepository projectRepository;

        // Repositorio para preparar las cuentas usadas por cada escenario de autorización.
        @Autowired
        private UserRepository userRepository;

        // Codificador real para que las cuentas de prueba puedan autenticarse por HTTP Basic.
        @Autowired
        private PasswordEncoder passwordEncoder;

    // Elimina datos persistidos para que el resultado de una prueba no afecte a otra.
    @BeforeEach
        void clearProjects() {
        // Borra las filas antes de probar un nuevo escenario HTTP.
        projectRepository.deleteAll();
                // Elimina las cuentas anteriores una vez borradas las referencias de proyectos.
                userRepository.deleteAll();
                // Crea el propietario que usarán las peticiones autenticadas de esta prueba.
                userRepository.save(new UserEntity("owner@example.com", passwordEncoder.encode("password123"), GlobalRole.MEMBER));
    }

    // Comprueba el flujo de creación, lectura, listado y archivado de un proyecto.
    @Test
    void createsReadsListsAndArchivesAProject() throws Exception {
        // Envía una petición válida y comprueba el recurso creado y su URL de consulta.
        String location = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"ProjectHub\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("ProjectHub"))
                .andExpect(jsonPath("$.status").value("ACTIVE"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Consulta el proyecto recién creado a través de la ubicación devuelta por la API.
        mockMvc.perform(get(location).with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("ProjectHub"));

        // Comprueba que el proyecto aparece en el listado antes de archivarlo.
        mockMvc.perform(get("/api/projects").with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("ProjectHub"));

        // Archiva el proyecto usando su recurso y confirma el estado final.
        mockMvc.perform(post(location + "/archive").with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("ARCHIVED"));
    }

    // Comprueba que un nombre vacío se rechaza antes de llegar a persistencia.
    @Test
    void rejectsBlankProjectNames() throws Exception {
        // Envía un nombre compuesto por espacios y verifica el HTTP 400 de validación.
        mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"   \"}"))
                .andExpect(status().isBadRequest());
    }

        // Comprueba que el límite de longitud del nombre también se aplica en la API.
        @Test
        void rejectsProjectNamesLongerThanOneHundredCharacters() throws Exception {
                // Genera un valor que supera en un carácter el máximo permitido.
                String longName = "a".repeat(101);
                // Inserta el valor en el JSON que enviará la petición HTTP.
                String requestBody = "{\"name\":\"" + longName + "\"}";

                // La validación del DTO debe rechazar el cuerpo antes de guardar datos.
                mockMvc.perform(post("/api/projects")
                                                .with(httpBasic("owner@example.com", "password123"))
                                                .contentType(APPLICATION_JSON)
                                                .content(requestBody))
                                .andExpect(status().isBadRequest());
        }

        // Comprueba que una consulta a un identificador ausente se traduce a HTTP 404.
        @Test
        void returnsNotFoundWhenProjectDoesNotExist() throws Exception {
                // Consulta un identificador que no se ha creado en esta prueba.
                mockMvc.perform(get("/api/projects/999999")
                                .with(httpBasic("owner@example.com", "password123")))
                                .andExpect(status().isNotFound());
        }

    // Comprueba que una cuenta autenticada no puede consultar proyectos ajenos.
    @Test
    void hidesAnotherUsersProject() throws Exception {
        // Crea un proyecto bajo la cuenta propietaria preparada para esta prueba.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Privado\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        // Añade una segunda identidad independiente para probar el límite de propiedad.
        userRepository.save(new UserEntity("other@example.com", passwordEncoder.encode("password123"), GlobalRole.MEMBER));

        // La consulta directa de un recurso ajeno se presenta como no encontrado.
        mockMvc.perform(get(projectLocation).with(httpBasic("other@example.com", "password123")))
                .andExpect(status().isNotFound());
        // Impide que otra cuenta cambie el nombre del proyecto ajeno.
        mockMvc.perform(patch(projectLocation)
                        .with(httpBasic("other@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Modificado\"}"))
                .andExpect(status().isNotFound());
        // El listado solo incluye proyectos propiedad de la cuenta autenticada.
        mockMvc.perform(get("/api/projects").with(httpBasic("other@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isEmpty());
    }

    // Comprueba que las rutas protegidas rechazan peticiones sin credenciales.
    @Test
    void rejectsAnonymousProjectRequests() throws Exception {
        // Intenta listar proyectos sin encabezado de autenticación.
        mockMvc.perform(get("/api/projects"))
                .andExpect(status().isUnauthorized());
    }

    // Comprueba que el propietario puede cambiar el nombre y que el cambio persiste.
    @Test
    void updatesOwnProjectName() throws Exception {
        // Crea el proyecto que se actualizará en esta prueba.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Inicial\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");

        // Envía el nuevo nombre y confirma la respuesta del endpoint PATCH.
        mockMvc.perform(patch(projectLocation)
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Actualizado\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Actualizado"));
        // Consulta el recurso para comprobar que el cambio se guardó en persistencia.
        mockMvc.perform(get(projectLocation).with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Actualizado"));
    }

    // Comprueba que un proyecto archivado no admite edición normal.
    @Test
    void doesNotUpdateArchivedProject() throws Exception {
        // Crea y archiva un proyecto propiedad de la cuenta autenticada.
        String projectLocation = mockMvc.perform(post("/api/projects")
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"Archivado\"}"))
                .andReturn()
                .getResponse()
                .getHeader("Location");
        mockMvc.perform(post(projectLocation + "/archive")
                        .with(httpBasic("owner@example.com", "password123")))
                .andExpect(status().isOk());

        // La edición responde conflicto y conserva el estado archivado.
        mockMvc.perform(patch(projectLocation)
                        .with(httpBasic("owner@example.com", "password123"))
                        .contentType(APPLICATION_JSON)
                        .content("{\"name\":\"No permitido\"}"))
                .andExpect(status().isConflict());
    }
}