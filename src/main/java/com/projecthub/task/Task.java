package com.projecthub.task;

// Representa una tarea del dominio de ProjectHub.
// La tarea está asociada a un proyecto y tiene un estado y un título.
public class Task {
    // Título de la tarea. Debe estar presente y respetar el máximo del dominio.
    private final String title;

    // Descripción opcional con una longitud máxima definida por el dominio.
    private final String description;

    // Estado actual de la tarea: TODO, IN_PROGRESS o DONE.
    private TaskStatus status = TaskStatus.TODO;

    // Prioridad de la tarea; las tareas nuevas usan MEDIUM si no se indica otra.
    private final TaskPriority priority;

    // Construye una tarea validando que el título tenga contenido útil.
    public Task(String title) {
        // Mantiene disponible la creación sencilla de tareas sin descripción.
        this(title, null, TaskPriority.MEDIUM);
    }

    // Construye una tarea validando descripción y prioridad además del título.
    public Task(String title, String description) {
        // Mantiene el valor inicial acordado cuando no se especifica prioridad.
        this(title, description, TaskPriority.MEDIUM);
    }

    // Construye una tarea validando título, descripción opcional y prioridad.
    public Task(String title, String description, TaskPriority priority) {
        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Task title must not be blank");
        }
        if (title.length() > 150) {
            throw new IllegalArgumentException("Task title must not exceed 150 characters");
        }
        if (description != null && description.length() > 2000) {
            throw new IllegalArgumentException("Task description must not exceed 2000 characters");
        }
        if (priority == null) {
            throw new IllegalArgumentException("Task priority must not be null");
        }
        this.title = title;
        this.description = description;
        this.priority = priority;
    }

    // Devuelve el título actual de la tarea.
    public String getTitle() {
        return title;
    }

    // Devuelve la descripción opcional de la tarea.
    public String getDescription() {
        return description;
    }

    // Devuelve la prioridad actual de la tarea.
    public TaskPriority getPriority() {
        return priority;
    }

    // Devuelve el estado actual de la tarea.
    public TaskStatus getStatus() {
        return status;
    }

    // Cambia el estado de la tarea a IN_PROGRESS si la transición es válida.
    public void start() {
        if (this.status == TaskStatus.DONE) {
            throw new IllegalStateException("A completed task cannot be reopened in the MVP");
        }
        if (this.status == TaskStatus.IN_PROGRESS) {
            return;
        }
        this.status = TaskStatus.IN_PROGRESS;
    }

    // Cambia el estado de la tarea a DONE si la transición es válida.
    public void complete() {
        if (this.status == TaskStatus.TODO) {
            throw new IllegalStateException("A task cannot be marked as DONE directly from TODO");
        }
        if (this.status == TaskStatus.DONE) {
            return;
        }
        this.status = TaskStatus.DONE;
    }

    // Devuelve una tarea en curso a TODO y rechaza la operación si ya está terminada.
    public void returnToTodo() {
        if (this.status == TaskStatus.DONE) {
            throw new IllegalStateException("A completed task cannot be returned to TODO");
        }
        if (this.status == TaskStatus.TODO) {
            return;
        }
        this.status = TaskStatus.TODO;
    }
}
