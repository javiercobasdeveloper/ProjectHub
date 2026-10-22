package com.projecthub.project.api;

// Importa el enum del dominio para devolver un estado tipado en la respuesta HTTP.
import com.projecthub.project.ProjectStatus;

// Define los datos públicos de un proyecto sin exponer la entidad JPA.
public record ProjectResponse(Long id, String name, ProjectStatus status) {
}