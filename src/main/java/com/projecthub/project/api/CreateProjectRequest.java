package com.projecthub.project.api;

// Importa validaciones Jakarta para rechazar nombres inválidos antes de ejecutar el caso de uso.
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Representa los datos permitidos al crear un proyecto desde una petición HTTP.
public record CreateProjectRequest(
        // Exige contenido real y limita el nombre al máximo definido por el dominio.
        @NotBlank @Size(max = 100) String name
) {
}