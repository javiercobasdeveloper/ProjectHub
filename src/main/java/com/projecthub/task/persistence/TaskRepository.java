package com.projecthub.task.persistence;

// Importa las operaciones CRUD de Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

// Spring genera la implementación de este repositorio durante el arranque.
public interface TaskRepository extends JpaRepository<TaskEntity, Long> {

    // Busca las tareas cuyo proyecto relacionado coincide con el identificador recibido.
    List<TaskEntity> findAllByProject_Id(Long projectId);

    // Lista tareas solo si el proyecto indicado pertenece al correo autenticado.
    List<TaskEntity> findAllByProject_IdAndProject_Owner_EmailIgnoreCase(Long projectId, String email);

    // Busca una tarea solo si el propietario de su proyecto coincide con el usuario.
    Optional<TaskEntity> findByIdAndProject_Owner_EmailIgnoreCase(Long id, String email);
}