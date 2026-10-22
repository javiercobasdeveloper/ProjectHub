package com.projecthub.user.api;

// Importa restricciones de formato y longitud para validar el alta HTTP.
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Define los datos permitidos para registrar una cuenta, sin incluir el rol.
public record RegisterUserRequest(
        @NotBlank @Email @Size(max = 254) String email,
        @NotBlank @Size(min = 8, max = 72) String password
) {
}