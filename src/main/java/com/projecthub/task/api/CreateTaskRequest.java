package com.projecthub.task.api;

// Importa la prioridad admitida al crear una tarea.
import com.projecthub.task.TaskPriority;
// Importa validaciones Jakarta para comprobar los datos antes de ejecutar el servicio.
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Define los datos que acepta la API al crear una tarea.
public record CreateTaskRequest(
        // Exige un título con contenido y limita su longitud al máximo del dominio.
        @NotBlank @Size(max = 150) String title,
        // Permite omitir la descripción y valida el límite del modelo de dominio.
        @Size(max = 2000) String description,
        // Permite indicar prioridad; el servicio usa MEDIUM si se omite.
        TaskPriority priority
) {
}