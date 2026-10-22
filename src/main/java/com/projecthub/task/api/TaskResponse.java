package com.projecthub.task.api;

// Importa el estado del dominio que se incluye en las respuestas de tarea.
import com.projecthub.task.TaskStatus;
import com.projecthub.task.TaskPriority;

// Representa una tarea y su proyecto sin exponer entidades ni relaciones JPA.
public record TaskResponse(
	Long id,
	Long projectId,
	String title,
	TaskStatus status,
	String description,
	TaskPriority priority,
	Long assigneeId
) {
}