package com.projecthub.project.api;

// Importa validaciones para comprobar el nuevo nombre antes del caso de uso.
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

// Define el nombre nuevo que acepta la operación PATCH de proyecto.
public record UpdateProjectRequest(
        @NotBlank @Size(max = 100) String name
) {
}