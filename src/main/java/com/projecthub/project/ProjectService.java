package com.projecthub.project;

// Importa los DTOs que separan el contrato HTTP de las clases de persistencia.
import com.projecthub.project.api.ProjectResponse;
// Importa la entidad que representa una fila de la tabla projects.
import com.projecthub.project.persistence.ProjectEntity;
// Importa el repositorio Spring Data para leer y guardar proyectos.
import com.projecthub.project.persistence.ProjectRepository;
import com.projecthub.user.persistence.UserEntity;
import com.projecthub.user.persistence.UserRepository;
import com.projecthub.user.UserNotFoundException;
// Importa la anotación que registra esta clase como servicio de aplicación.
import org.springframework.stereotype.Service;
// Importa la anotación que agrupa cada operación de escritura en una transacción.
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

// Coordina las reglas del dominio y el acceso a datos para los casos de uso de proyectos.
@Service
public class ProjectService {

    // Repositorio que persiste y consulta las entidades de proyecto.
    private final ProjectRepository projectRepository;
    // Repositorio que resuelve la cuenta autenticada para asignarla como propietaria.
    private final UserRepository userRepository;

    // Recibe el repositorio mediante inyección de dependencias de Spring.
    public ProjectService(ProjectRepository projectRepository, UserRepository userRepository) {
        // Conserva la dependencia para usarla en los casos de uso.
        this.projectRepository = projectRepository;
        // Conserva el repositorio de usuarios para asociar el propietario del proyecto.
        this.userRepository = userRepository;
    }

    // Valida el nombre con el modelo de dominio, persiste el proyecto y devuelve su vista pública.
    @Transactional
    public ProjectResponse create(String name, String ownerEmail) {
        // El constructor de dominio rechaza nombres nulos, en blanco o demasiado largos.
        Project project = new Project(name);
        // Resuelve la identidad autenticada que debe quedar como propietaria.
        UserEntity owner = userRepository.findByEmailIgnoreCase(ownerEmail)
            .orElseThrow(() -> new UserNotFoundException(ownerEmail));
        // Copia los datos validados al modelo JPA que conoce la base de datos.
        ProjectEntity entity = new ProjectEntity(project.getName(), project.getStatus(), owner);
        // Guarda la entidad para que la base de datos asigne su identificador.
        ProjectEntity savedEntity = projectRepository.save(entity);
        // Convierte la entidad persistida al contrato de respuesta de la API.
        return toResponse(savedEntity);
    }

    // Devuelve todos los proyectos usando DTOs en lugar de exponer entidades JPA.
    @Transactional(readOnly = true)
    public List<ProjectResponse> findAll(String ownerEmail) {
        // Consulta las filas y transforma cada una en una respuesta pública.
        return projectRepository.findAllByOwner_EmailIgnoreCase(ownerEmail).stream()
                .map(this::toResponse)
                .toList();
    }

    // Busca un proyecto por identificador o comunica que no existe.
    @Transactional(readOnly = true)
    public ProjectResponse findById(Long id, String ownerEmail) {
        // Recupera la entidad y convierte el resultado al contrato de la API.
        return toResponse(findEntityById(id, ownerEmail));
    }

    // Archiva un proyecto mediante la regla del dominio y guarda el nuevo estado.
    @Transactional
    public ProjectResponse archive(Long id, String ownerEmail) {
        // Obtiene la entidad persistida o lanza un error 404 si no existe.
        ProjectEntity entity = findEntityById(id, ownerEmail);
        // Reconstruye el objeto de dominio para aplicar el cambio de estado permitido.
        Project project = new Project(entity.getName());
        // El método del dominio es idempotente y cambia el estado a ARCHIVED.
        project.archive();
        // Copia el estado resultante al modelo persistente.
        entity.setStatus(project.getStatus());
        // Guarda la modificación antes de responder al cliente.
        ProjectEntity savedEntity = projectRepository.save(entity);
        // Devuelve la representación actualizada del proyecto.
        return toResponse(savedEntity);
    }

    // Actualiza el nombre de un proyecto activo que pertenezca al usuario autenticado.
    @Transactional
    public ProjectResponse update(Long id, String name, String ownerEmail) {
        // Restringe la búsqueda al propietario para no modificar recursos ajenos.
        ProjectEntity entity = findEntityById(id, ownerEmail);
        // Evita escrituras normales sobre proyectos archivados.
        if (entity.getStatus() == ProjectStatus.ARCHIVED) {
            throw new IllegalStateException("An archived project cannot be edited");
        }
        // Reutiliza la regla de dominio para validar el nombre nuevo.
        Project updatedProject = new Project(name);
        // Copia el nombre ya validado a la entidad que JPA persiste.
        entity.setName(updatedProject.getName());
        // Guarda y devuelve la representación pública actualizada.
        return toResponse(projectRepository.save(entity));
    }

    // Centraliza la búsqueda para mantener igual el comportamiento de los casos de uso.
    private ProjectEntity findEntityById(Long id, String ownerEmail) {
        // Convierte un resultado ausente o ajeno en la misma respuesta HTTP 404.
        return projectRepository.findByIdAndOwner_EmailIgnoreCase(id, ownerEmail)
                .orElseThrow(() -> new ProjectNotFoundException(id));
    }

    // Traduce una entidad interna al DTO que forma parte del contrato público.
    private ProjectResponse toResponse(ProjectEntity entity) {
        // Copia solo los campos que la API debe exponer.
        return new ProjectResponse(entity.getId(), entity.getName(), entity.getStatus());
    }
}