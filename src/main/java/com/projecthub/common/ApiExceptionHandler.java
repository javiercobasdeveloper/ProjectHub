package com.projecthub.common;

// Importa la excepción lanzada cuando se intenta reutilizar un correo registrado.
import com.projecthub.user.EmailAlreadyRegisteredException;
import com.projecthub.user.UserNotFoundException;
// Importa tipos para crear respuestas Problem Details con HTTP 409 Conflict.
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
// Importa anotaciones para interceptar excepciones lanzadas por cualquier controlador REST.
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

// Convierte errores de transición del dominio en respuestas HTTP comprensibles para el cliente.
@RestControllerAdvice
public class ApiExceptionHandler {

    // Responde con HTTP 409 cuando una transición de estado no está permitida.
    @ExceptionHandler(IllegalStateException.class)
    public ProblemDetail handleIllegalState(IllegalStateException exception) {
        // Construye un cuerpo estándar que explica el conflicto sin ocultar la regla incumplida.
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    // Responde HTTP 400 cuando una regla del dominio rechaza datos de entrada.
    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail handleIllegalArgument(IllegalArgumentException exception) {
        // Convierte los datos inválidos del dominio en un error de cliente.
        return ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
    }

    // Responde HTTP 409 cuando el correo ya está asociado a otra cuenta.
    @ExceptionHandler(EmailAlreadyRegisteredException.class)
    public ProblemDetail handleEmailAlreadyRegistered(EmailAlreadyRegisteredException exception) {
        // Construye una respuesta estándar de conflicto sin exponer credenciales.
        return ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, exception.getMessage());
    }

    // Responde HTTP 404 si la cuenta requerida ya no está disponible.
    @ExceptionHandler(UserNotFoundException.class)
    public ProblemDetail handleUserNotFound(UserNotFoundException exception) {
        // Construye una respuesta estándar sin revelar información de autenticación.
        return ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
    }
}