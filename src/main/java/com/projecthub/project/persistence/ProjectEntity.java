package com.projecthub.project.persistence;

// Importa anotaciones JPA para mapear esta clase a una tabla relacional.
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

// Importa el enum del dominio que limita los estados posibles del proyecto.
import com.projecthub.project.ProjectStatus;
import com.projecthub.user.persistence.UserEntity;

// Representa la fila persistida de un proyecto; la API no expondrá esta entidad directamente.
@Entity
@Table(name = "projects")
public class ProjectEntity {

    // La base de datos genera el identificador al insertar una fila nueva.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Limita el nombre en la base de datos además de validarlo en el dominio y la API.
    @Column(nullable = false, length = 100)
    private String name;

    // Guarda el nombre del enum como texto legible y estable en la tabla.
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ProjectStatus status;

    // Conserva quién creó el proyecto y permite filtrar recursos por propietario.
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id")
    private UserEntity owner;

    // JPA necesita un constructor sin argumentos para reconstruir objetos desde filas.
    protected ProjectEntity() {
    }

    // Crea la representación persistente a partir de los datos validados del dominio.
    public ProjectEntity(String name, ProjectStatus status, UserEntity owner) {
        // Conserva el nombre que el servicio ya validó usando el modelo de dominio.
        this.name = name;
        // Conserva el estado actual del proyecto para almacenarlo junto al nombre.
        this.status = status;
        // Asocia el proyecto con el usuario autenticado que lo creó.
        this.owner = owner;
    }

    // Devuelve el identificador asignado por la base de datos.
    public Long getId() {
        return id;
    }

    // Devuelve el nombre persistido del proyecto.
    public String getName() {
        return name;
    }

    // Actualiza el nombre después de que el servicio lo valide en el dominio.
    public void setName(String name) {
        this.name = name;
    }

    // Devuelve el estado persistido del proyecto.
    public ProjectStatus getStatus() {
        return status;
    }

    // Devuelve la cuenta propietaria para asignarla como responsable inicial de tareas.
    public UserEntity getOwner() {
        return owner;
    }

    // Actualiza el estado que el servicio ya ha cambiado mediante una regla de dominio.
    public void setStatus(ProjectStatus status) {
        this.status = status;
    }
}