package com.projecthub.project;

// Define los posibles estados que puede tener un proyecto dentro del dominio.
// ACTIVE significa que el proyecto sigue vivo y operativo.
// ARCHIVED significa que el proyecto ya terminó y se archivó en lugar de borrarse.
public enum ProjectStatus {
    ACTIVE,
    ARCHIVED
}
