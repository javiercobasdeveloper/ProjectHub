package com.projecthub.project;

// La palabra package indica en qué paquete está la clase dentro del proyecto.
// En Java, el paquete organiza el código y evita conflictos entre clases con el mismo nombre.
// Aquí: com.projecthub.project significa que esta clase pertenece al módulo 'project' del proyecto ProjectHub.

// Representa un proyecto del dominio, independiente de Spring y de la base de datos.
public class Project {
    // Conserva el nombre validado y evita que se reasigne después de construir el proyecto.
    private final String name;

    // Indica si el proyecto está activo o archivado.
    // El valor inicial es ACTIVE para que un proyecto recién creado siga operativo.
    private ProjectStatus status = ProjectStatus.ACTIVE;

    // Construye el proyecto y comprueba su regla de nombre antes de guardar el dato.
    public Project(String name) {
        // Rechaza tanto la ausencia del nombre como un texto compuesto solo por espacios.
        if (name == null || name.isBlank()) {
            // Informa al código que intenta crear el proyecto de que el dato no es válido.
            throw new IllegalArgumentException("Project name must not be blank");
        }
        // Impide que el nombre supere el máximo establecido por los requisitos.
        if (name.length() > 100) {
            // Informa al código que intenta crear el proyecto de que el dato es demasiado largo.
            throw new IllegalArgumentException("Project name must not exceed 100 characters");
        }
        // Guarda en el atributo el nombre que ya superó las validaciones anteriores.
        this.name = name;
    }

    // Permite consultar el nombre sin exponer el atributo para que se pueda modificar desde fuera.
    public String getName() {
        // Devuelve el nombre almacenado en este proyecto.
        return name;
    }

    // Devuelve el estado actual del proyecto para que la lógica del dominio pueda consultarlo.
    public ProjectStatus getStatus() {
        // Expone el valor del estado sin permitir modificación directa desde fuera.
        return status;
    }

    // Cambia el estado del proyecto a ARCHIVED, manteniendo el historial de la entidad en el dominio.
    public void archive() {
        // Si el proyecto ya estaba archivado, no hay que cambiar nada más.
        if (this.status == ProjectStatus.ARCHIVED) {
            return;
        }
        // El proyecto pasa a archivado cuando se cierra o finaliza.
        this.status = ProjectStatus.ARCHIVED;
    }
}