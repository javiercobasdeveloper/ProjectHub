package com.projecthub.project.api;

// Importa el servicio que ejecuta los casos de uso fuera de la capa HTTP.
import com.projecthub.project.ProjectService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
// Importa la validación de parámetros del cuerpo de la petición.
import jakarta.validation.Valid;
// Importa tipos HTTP para responder con estado y cabeceras adecuados.
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
// Importa anotaciones MVC para declarar rutas y enlazar datos de la petición.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

// Expone las operaciones HTTP relacionadas con proyectos.
@RestController
@RequestMapping("/api/projects")
@Tag(name = "Proyectos", description = "Creación, consulta y archivado de proyectos propios")
@SecurityRequirement(name = "basicAuth")
public class ProjectController {

    // Servicio de aplicación que contiene los casos de uso de proyectos.
    private final ProjectService projectService;

    // Recibe el servicio mediante inyección de dependencias de Spring.
    public ProjectController(ProjectService projectService) {
        // Conserva el servicio para delegar las peticiones recibidas.
        this.projectService = projectService;
    }

    // Crea un proyecto y devuelve HTTP 201 con la ubicación del nuevo recurso.
    @PostMapping
    @Operation(summary = "Crear proyecto", description = "Crea un proyecto asociado al usuario autenticado.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Proyecto creado"),
            @ApiResponse(responseCode = "400", description = "El nombre no es válido"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación")
        })
    public ResponseEntity<ProjectResponse> create(
            @Valid @RequestBody CreateProjectRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Ejecuta el caso de uso con el nombre ya validado por Jakarta Validation.
        ProjectResponse response = projectService.create(request.name(), currentUser.getUsername());
        // Construye la URL relativa para que el cliente pueda consultar el recurso creado.
        URI location = URI.create("/api/projects/" + response.id());
        // Devuelve Created y el cuerpo con la representación del nuevo proyecto.
        return ResponseEntity.created(location).body(response);
    }

    // Lista los proyectos disponibles para la aplicación.
    @GetMapping
    @Operation(summary = "Listar proyectos propios")
    public List<ProjectResponse> findAll(@AuthenticationPrincipal UserDetails currentUser) {
        // Delega la consulta y devuelve únicamente DTOs públicos.
        return projectService.findAll(currentUser.getUsername());
    }

    // Consulta un proyecto por su identificador.
    @GetMapping("/{id}")
    @Operation(summary = "Consultar proyecto propio")
    public ProjectResponse findById(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Delega la búsqueda; el servicio genera un 404 si el identificador no existe.
        return projectService.findById(id, currentUser.getUsername());
    }

    // Archiva un proyecto sin borrar su información.
    @PostMapping("/{id}/archive")
    @Operation(summary = "Archivar proyecto propio")
    public ProjectResponse archive(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Delega el cambio al caso de uso que aplica la regla de dominio.
        return projectService.archive(id, currentUser.getUsername());
    }

    // Actualiza el nombre de un proyecto propio que todavía esté activo.
    @PatchMapping("/{id}")
    @Operation(summary = "Editar proyecto propio", description = "Actualiza el nombre de un proyecto activo.")
    public ProjectResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateProjectRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Delega la validación de dominio y la comprobación de propiedad al servicio.
        return projectService.update(id, request.name(), currentUser.getUsername());
    }
}