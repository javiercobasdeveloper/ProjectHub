package com.projecthub.task.persistence;

// Importa anotaciones JPA para relacionar esta entidad con las tablas de tareas y proyectos.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Importa la entidad del proyecto al que pertenece esta tarea.
import com.projecthub.project.persistence.ProjectEntity;
// Importa el enum del dominio para limitar los estados persistidos.
import com.projecthub.task.TaskStatus;
import com.projecthub.task.TaskPriority;
import com.projecthub.user.persistence.UserEntity;

// Representa una fila persistida de tarea; las respuestas HTTP usan un DTO independiente.
@Entity
@Table(name = "tasks")
public class TaskEntity {

    // La base de datos asigna el identificador al insertar la tarea.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Limita el título en el esquema además de validarlo antes de persistir.
    @Column(nullable = false, length = 150)
    private String title;

    // Guarda una descripción opcional con el límite que valida el modelo de dominio.
    @Column(length = 2000)
    private String description;

    // Persiste el estado como texto legible y no como un ordinal frágil.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskStatus status;

    // Guarda la prioridad como texto legible y estable en la base de datos.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TaskPriority priority;

    // Cada tarea pertenece a un proyecto; cargar el proyecto se difiere hasta que sea necesario.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "project_id", nullable = false)
    private ProjectEntity project;

    // Cada tarea nueva se asigna inicialmente al propietario de su proyecto.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "assignee_id")
    private UserEntity assignee;

    // JPA necesita este constructor para reconstruir una tarea desde una fila de la base de datos.
    protected TaskEntity() {
    }

    // Construye una entidad asociada al proyecto y con estado inicial TODO.
        public TaskEntity(
            String title,
            String description,
            TaskStatus status,
            TaskPriority priority,
            ProjectEntity project,
            UserEntity assignee
        ) {
        // Conserva el título validado por el modelo de dominio.
        this.title = title;
        // Conserva la descripción opcional validada por el modelo de dominio.
        this.description = description;
        // Conserva el estado que el dominio permite persistir.
        this.status = status;
        // Conserva la prioridad validada por el modelo de dominio.
        this.priority = priority;
        // Asocia la tarea con el proyecto propietario.
        this.project = project;
        // Asigna inicialmente la tarea al propietario del proyecto.
        this.assignee = assignee;
    }

    // Devuelve el identificador generado por la base de datos.
    public Long getId() {
        return id;
    }

    // Devuelve el título guardado.
    public String getTitle() {
        return title;
    }

    // Devuelve la descripción persistida de la tarea.
    public String getDescription() {
        return description;
    }

    // Devuelve el estado guardado.
    public TaskStatus getStatus() {
        return status;
    }

    // Devuelve la prioridad persistida de la tarea.
    public TaskPriority getPriority() {
        return priority;
    }

    // Devuelve la cuenta asignada a la tarea.
    public UserEntity getAssignee() {
        return assignee;
    }

    // Actualiza el título previamente validado por el servicio.
    public void setTitle(String title) {
        this.title = title;
    }

    // Actualiza la descripción previamente validada por el servicio.
    public void setDescription(String description) {
        this.description = description;
    }

    // Actualiza la prioridad previamente validada por el servicio.
    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    // Devuelve el proyecto propietario de la tarea.
    public ProjectEntity getProject() {
        return project;
    }

    // Actualiza el estado después de ejecutar la transición correspondiente en el dominio.
    public void setStatus(TaskStatus status) {
        this.status = status;
    }
}