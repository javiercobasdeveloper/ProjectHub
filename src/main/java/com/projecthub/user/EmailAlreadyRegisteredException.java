package com.projecthub.user;

// Indica que el correo solicitado ya identifica otra cuenta registrada.
public class EmailAlreadyRegisteredException extends RuntimeException {

    // Conserva el correo duplicado para que el error HTTP pueda explicarlo.
    public EmailAlreadyRegisteredException(String email) {
        // Inicializa el mensaje que el manejador global convierte en HTTP 409.
        super("Ya existe una cuenta con el correo " + email);
    }
}