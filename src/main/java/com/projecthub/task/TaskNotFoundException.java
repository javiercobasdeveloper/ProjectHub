package com.projecthub.task;

// Importa la anotación que traduce esta excepción a una respuesta HTTP 404.
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// Indica que la tarea solicitada no existe.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class TaskNotFoundException extends RuntimeException {

    // Construye un mensaje que identifica la tarea que no pudo encontrarse.
    public TaskNotFoundException(Long id) {
        // Incluye el identificador solicitado para facilitar el diagnóstico.
        super("Task " + id + " was not found");
    }
}