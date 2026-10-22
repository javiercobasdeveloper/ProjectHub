package com.projecthub.task.api;

// Importa el servicio que coordina reglas de dominio y persistencia.
import com.projecthub.task.TaskService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
// Importa la validación del cuerpo de la petición antes de llamar al servicio.
import jakarta.validation.Valid;
// Importa tipos HTTP para expresar la creación de recursos correctamente.
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
// Importa anotaciones MVC para rutas y parámetros HTTP.
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;
import java.util.List;

// Expone operaciones HTTP para crear y cambiar el estado de tareas.
@RestController
@Tag(name = "Tareas", description = "Creación, consulta y transiciones de tareas de proyectos propios")
@SecurityRequirement(name = "basicAuth")
public class TaskController {

    // Servicio de aplicación con los casos de uso de tareas.
    private final TaskService taskService;

    // Recibe el servicio de tareas mediante inyección de dependencias.
    public TaskController(TaskService taskService) {
        // Conserva el servicio para delegar las peticiones recibidas.
        this.taskService = taskService;
    }

    // Crea una tarea dentro del proyecto indicado y devuelve HTTP 201.
    @PostMapping("/api/projects/{projectId}/tasks")
    @Operation(summary = "Crear tarea", description = "Crea una tarea dentro de un proyecto propiedad del usuario autenticado.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Tarea creada"),
            @ApiResponse(responseCode = "400", description = "El título no es válido"),
            @ApiResponse(responseCode = "401", description = "Se requiere autenticación"),
            @ApiResponse(responseCode = "404", description = "El proyecto no existe o no pertenece al usuario")
        })
    public ResponseEntity<TaskResponse> create(
            @PathVariable Long projectId,
            @Valid @RequestBody CreateTaskRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Ejecuta el caso de uso con datos que ya pasaron la validación del DTO.
        TaskResponse response = taskService.create(
            projectId,
            request.title(),
            request.description(),
            request.priority(),
            currentUser.getUsername()
        );
        // Construye la URL del recurso de tarea que se acaba de crear.
        URI location = URI.create("/api/tasks/" + response.id());
        // Devuelve Created y la representación pública de la tarea.
        return ResponseEntity.created(location).body(response);
    }

    // Lista las tareas asociadas al proyecto solicitado.
    @GetMapping("/api/projects/{projectId}/tasks")
    @Operation(summary = "Listar tareas de un proyecto propio")
    public List<TaskResponse> findAllByProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Delega la consulta y devuelve DTOs, no entidades de persistencia.
        return taskService.findAllByProject(projectId, currentUser.getUsername());
    }

    // Consulta una tarea por identificador.
    @GetMapping("/api/tasks/{id}")
    @Operation(summary = "Consultar tarea propia")
    public TaskResponse findById(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        // Delega la búsqueda y deja que el servicio responda 404 si no existe.
        return taskService.findById(id, currentUser.getUsername());
    }

    // Intenta pasar una tarea a IN_PROGRESS.
    @PostMapping("/api/tasks/{id}/start")
    @Operation(summary = "Iniciar tarea propia")
    public TaskResponse start(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        // Delega la transición al servicio y al modelo de dominio.
        return taskService.start(id, currentUser.getUsername());
    }

    // Intenta pasar una tarea a DONE.
    @PostMapping("/api/tasks/{id}/complete")
    @Operation(summary = "Completar tarea propia")
    public TaskResponse complete(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        // Delega la transición al servicio y al modelo de dominio.
        return taskService.complete(id, currentUser.getUsername());
    }

    // Devuelve a TODO una tarea que esté en curso.
    @PostMapping("/api/tasks/{id}/return-to-todo")
    @Operation(summary = "Devolver tarea propia a TODO")
    public TaskResponse returnToTodo(@PathVariable Long id, @AuthenticationPrincipal UserDetails currentUser) {
        // Delega la transición permitida al servicio y al modelo de dominio.
        return taskService.returnToTodo(id, currentUser.getUsername());
    }

    // Actualiza los campos indicados de una tarea propia sin alterar su estado.
    @PatchMapping("/api/tasks/{id}")
    @Operation(summary = "Editar tarea propia", description = "Actualiza título, descripción o prioridad sin cambiar el estado ni el responsable.")
    public TaskResponse update(
            @PathVariable Long id,
            @Valid @RequestBody UpdateTaskRequest request,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Delega propiedad, validación de dominio y persistencia al servicio.
        return taskService.update(
                id,
                request.title(),
                request.description(),
                request.priority(),
                currentUser.getUsername()
        );
    }

    // Elimina una tarea propia y confirma la operación con HTTP 204.
    @DeleteMapping("/api/tasks/{id}")
    @Operation(summary = "Eliminar tarea propia", description = "Elimina una tarea de un proyecto activo propiedad del usuario autenticado.")
    @ApiResponse(responseCode = "204", description = "Tarea eliminada")
    public ResponseEntity<Void> delete(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser
    ) {
        // Delega la comprobación de propiedad y las reglas de archivado al servicio.
        taskService.delete(id, currentUser.getUsername());
        // Devuelve No Content al completar el borrado.
        return ResponseEntity.noContent().build();
    }
}