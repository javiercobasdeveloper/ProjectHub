package com.projecthub.user.persistence;

// Importa el repositorio CRUD de Spring Data JPA.
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

// Spring genera consultas tipadas para localizar usuarios por su correo.
public interface UserRepository extends JpaRepository<UserEntity, Long> {

    // Busca sin distinguir mayúsculas para que el correo tenga inicio de sesión consistente.
    Optional<UserEntity> findByEmailIgnoreCase(String email);

    // Comprueba duplicados sin distinguir mayúsculas antes de registrar una cuenta.
    boolean existsByEmailIgnoreCase(String email);
}