package com.projecthub.user;

// Indica que no se encontró la cuenta requerida por una operación autenticada.
public class UserNotFoundException extends RuntimeException {

    // Conserva el correo solicitado para producir una respuesta HTTP 404 comprensible.
    public UserNotFoundException(String email) {
        // Inicializa el mensaje que recibirá el manejador de errores de la API.
        super("No se encontró el usuario " + email);
    }
}