package com.projecthub.user.api;

// Importa el rol público de la cuenta sin exponer datos de autenticación.
import com.projecthub.user.GlobalRole;

// Representa una cuenta en HTTP sin incluir la contraseña ni su hash.
public record UserResponse(Long id, String email, GlobalRole globalRole) {
}