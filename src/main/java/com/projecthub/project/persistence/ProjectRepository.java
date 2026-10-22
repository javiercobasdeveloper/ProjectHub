package com.projecthub.project.persistence;

// Importa el repositorio base de Spring Data JPA con operaciones CRUD tipadas.
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Spring crea la implementación de acceso a datos durante el arranque de la aplicación.
public interface ProjectRepository extends JpaRepository<ProjectEntity, Long> {

	// Lista solo proyectos cuyo propietario coincide con el correo autenticado.
	List<ProjectEntity> findAllByOwner_EmailIgnoreCase(String email);

	// Busca un proyecto por ID siempre que pertenezca al usuario autenticado.
	Optional<ProjectEntity> findByIdAndOwner_EmailIgnoreCase(Long id, String email);
}