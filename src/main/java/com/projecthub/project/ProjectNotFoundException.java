package com.projecthub.project;

// Importa la anotación que convierte esta excepción en una respuesta HTTP 404.
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Indica que el proyecto solicitado no existe en la persistencia.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ProjectNotFoundException extends RuntimeException {

    // Construye un mensaje útil para identificar el recurso ausente.
    public ProjectNotFoundException(Long id) {
        // Conserva el identificador solicitado dentro de la respuesta de error.
        super("Project " + id + " was not found");
    }
}