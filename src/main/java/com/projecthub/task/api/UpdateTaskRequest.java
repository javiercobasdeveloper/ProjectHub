package com.projecthub.task.api;

// Importa la prioridad y validaciones para campos opcionales de PATCH.
import com.projecthub.task.TaskPriority;
import jakarta.validation.constraints.Size;

// Define campos opcionales; los valores nulos conservan los datos actuales.
public record UpdateTaskRequest(
        @Size(max = 150) String title,
        @Size(max = 2000) String description,
        TaskPriority priority
) {
}