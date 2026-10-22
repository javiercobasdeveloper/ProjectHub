package com.projecthub.task;

// Importa el DTO que representa una tarea en la API.
import com.projecthub.task.api.TaskResponse;
// Importa la entidad persistente de tarea.
import com.projecthub.task.persistence.TaskEntity;
// Importa el repositorio de tareas.
import com.projecthub.task.persistence.TaskRepository;
// Importa el acceso a proyectos para comprobar la relación antes de crear o listar tareas.
import com.projecthub.project.ProjectNotFoundException;
import com.projecthub.project.ProjectStatus;
import com.projecthub.project.persistence.ProjectEntity;
import com.projecthub.project.persistence.ProjectRepository;
// Importa anotaciones de Spring para registrar el servicio y controlar transacciones.
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Coordina las reglas de estado del dominio con las entidades persistidas de tarea.
@Service
public class TaskService {

    // Repositorio que permite almacenar y consultar tareas.
    private final TaskRepository taskRepository;
    // Repositorio que valida la existencia del proyecto relacionado.
    private final ProjectRepository projectRepository;

    // Recibe los repositorios requeridos por los casos de uso de tareas.
    public TaskService(TaskRepository taskRepository, ProjectRepository projectRepository) {
        // Conserva el repositorio de tareas para las operaciones de lectura y escritura.
        this.taskRepository = taskRepository;
        // Conserva el repositorio de proyectos para comprobar asociaciones válidas.
        this.projectRepository = projectRepository;
    }

    // Crea una tarea en un proyecto existente después de validar el título en el dominio.
    @Transactional
        public TaskResponse create(
            Long projectId,
            String title,
            String description,
            TaskPriority priority,
            String ownerEmail
        ) {
        // Recupera el proyecto o produce un 404 si el cliente indicó uno inexistente.
        ProjectEntity project = projectRepository.findByIdAndOwner_EmailIgnoreCase(projectId, ownerEmail)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        // Impide añadir trabajo nuevo a un proyecto que ya se ha archivado.
        if (project.getStatus() == ProjectStatus.ARCHIVED) {
            throw new IllegalStateException("A task cannot be created in an archived project");
        }
        // Aplica las reglas de título y establece el estado inicial TODO.
        // Usa MEDIUM cuando el cliente no indica una prioridad.
        Task task = new Task(title, description, priority == null ? TaskPriority.MEDIUM : priority);
        // Construye la entidad que JPA almacenará junto a su proyecto propietario.
        TaskEntity entity = new TaskEntity(
            task.getTitle(),
            task.getDescription(),
            task.getStatus(),
            task.getPriority(),
            project,
            project.getOwner()
        );
        // Persiste la tarea para obtener su identificador.
        TaskEntity savedEntity = taskRepository.save(entity);
        // Devuelve el DTO público sin exponer la entidad JPA.
        return toResponse(savedEntity);
    }

    // Lista las tareas de un proyecto y responde 404 si el proyecto no existe.
    @Transactional(readOnly = true)
    public List<TaskResponse> findAllByProject(Long projectId, String ownerEmail) {
        // Comprueba primero el proyecto para diferenciar una lista vacía de un padre inexistente.
        projectRepository.findByIdAndOwner_EmailIgnoreCase(projectId, ownerEmail)
                .orElseThrow(() -> new ProjectNotFoundException(projectId));
        // Consulta las tareas asociadas y transforma cada entidad en su DTO.
        return taskRepository.findAllByProject_IdAndProject_Owner_EmailIgnoreCase(projectId, ownerEmail).stream()
                .map(this::toResponse)
                .toList();
    }

    // Busca una tarea por identificador o responde que no existe.
    @Transactional(readOnly = true)
    public TaskResponse findById(Long id, String ownerEmail) {
        // Obtiene la entidad o produce el error 404 definido para tareas.
        TaskEntity entity = taskRepository.findByIdAndProject_Owner_EmailIgnoreCase(id, ownerEmail)
                .orElseThrow(() -> new TaskNotFoundException(id));
        // Convierte el resultado al formato público de la API.
        return toResponse(entity);
    }

    // Cambia una tarea a IN_PROGRESS si el dominio permite la transición.
    @Transactional
    public TaskResponse start(Long id, String ownerEmail) {
        // Recupera la tarea persistida o responde 404.
        TaskEntity entity = findEntityById(id, ownerEmail);
        // Reconstruye el estado del dominio, aplica la transición y guarda el nuevo estado.
        Task task = toDomain(entity);
        task.start();
        entity.setStatus(task.getStatus());
        // Persiste el cambio de estado antes de construir la respuesta.
        return toResponse(taskRepository.save(entity));
    }

    // Cambia una tarea a DONE solo si está en IN_PROGRESS.
    @Transactional
    public TaskResponse complete(Long id, String ownerEmail) {
        // Recupera la tarea persistida o responde 404.
        TaskEntity entity = findEntityById(id, ownerEmail);
        // Delega la regla de transición al modelo de dominio.
        Task task = toDomain(entity);
        task.complete();
        entity.setStatus(task.getStatus());
        // Guarda el resultado para que el nuevo estado sobreviva a nuevas peticiones.
        return toResponse(taskRepository.save(entity));
    }

    // Devuelve una tarea en curso a TODO mediante la transición del dominio.
    @Transactional
    public TaskResponse returnToTodo(Long id, String ownerEmail) {
        // Recupera la tarea persistida o responde 404.
        TaskEntity entity = findEntityById(id, ownerEmail);
        // Ejecuta la transición permitida y copia el estado resultante a la entidad JPA.
        Task task = toDomain(entity);
        task.returnToTodo();
        entity.setStatus(task.getStatus());
        // Persiste el estado devuelto a TODO.
        return toResponse(taskRepository.save(entity));
    }

    // Actualiza los campos enviados de una tarea propia sin modificar los omitidos.
    @Transactional
    public TaskResponse update(
            Long id,
            String title,
            String description,
            TaskPriority priority,
            String ownerEmail
    ) {
        // Restringe la búsqueda a tareas de proyectos propiedad del usuario autenticado.
        TaskEntity entity = findEntityById(id, ownerEmail);
        // Evita escrituras en tareas de proyectos archivados.
        if (entity.getProject().getStatus() == com.projecthub.project.ProjectStatus.ARCHIVED) {
            throw new IllegalStateException("A task in an archived project cannot be edited");
        }
        // Conserva los valores actuales de los campos que el cliente omitió.
        String updatedTitle = title == null ? entity.getTitle() : title;
        String updatedDescription = description == null ? entity.getDescription() : description;
        TaskPriority updatedPriority = priority == null ? entity.getPriority() : priority;
        // Reutiliza las reglas del dominio sobre el conjunto final de valores.
        Task updatedTask = new Task(updatedTitle, updatedDescription, updatedPriority);
        // Copia el título validado a la entidad persistente.
        entity.setTitle(updatedTask.getTitle());
        // Copia la descripción validada; una cadena vacía permite borrarla.
        entity.setDescription(updatedTask.getDescription());
        // Copia la prioridad validada a la entidad persistente.
        entity.setPriority(updatedTask.getPriority());
        // Guarda el recurso actualizado y devuelve su DTO público.
        return toResponse(taskRepository.save(entity));
    }

    // Elimina una tarea propia solo cuando su proyecto todavía está activo.
    @Transactional
    public void delete(Long id, String ownerEmail) {
        // Restringe la búsqueda al propietario para impedir borrar tareas ajenas.
        TaskEntity entity = findEntityById(id, ownerEmail);
        // Mantiene la regla de que los proyectos archivados no admiten escrituras normales.
        if (entity.getProject().getStatus() == com.projecthub.project.ProjectStatus.ARCHIVED) {
            throw new IllegalStateException("A task in an archived project cannot be deleted");
        }
        // Elimina únicamente la entidad cuya propiedad ya fue comprobada.
        taskRepository.delete(entity);
    }

    // Centraliza la búsqueda por identificador para los casos de uso de escritura.
    private TaskEntity findEntityById(Long id, String ownerEmail) {
        // Convierte un resultado ausente o ajeno en la misma respuesta HTTP 404.
        return taskRepository.findByIdAndProject_Owner_EmailIgnoreCase(id, ownerEmail)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    // Reconstruye las transiciones necesarias para obtener el estado de dominio almacenado.
    private Task toDomain(TaskEntity entity) {
        // El constructor valida el título y crea el estado inicial TODO.
        Task task = new Task(entity.getTitle(), entity.getDescription(), entity.getPriority());
        // Reproduce la transición válida que lleva de TODO al estado persistido.
        if (entity.getStatus() == TaskStatus.IN_PROGRESS || entity.getStatus() == TaskStatus.DONE) {
            task.start();
        }
        // Completa la secuencia válida si la base de datos indica que la tarea está terminada.
        if (entity.getStatus() == TaskStatus.DONE) {
            task.complete();
        }
        // Devuelve el objeto con un estado alcanzado mediante las reglas del dominio.
        return task;
    }

    // Traduce una entidad de tarea al contrato público de la API.
    private TaskResponse toResponse(TaskEntity entity) {
        // Expone únicamente el identificador del proyecto, no la entidad relacionada.
        return new TaskResponse(
            entity.getId(),
            entity.getProject().getId(),
            entity.getTitle(),
            entity.getStatus(),
            entity.getDescription(),
            entity.getPriority(),
            entity.getAssignee() == null ? null : entity.getAssignee().getId()
        );
    }
}