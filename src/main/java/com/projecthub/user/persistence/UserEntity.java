package com.projecthub.user.persistence;

// Importa las anotaciones JPA que relacionan esta clase con la tabla de usuarios.
import com.projecthub.user.GlobalRole;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

// Representa una cuenta persistida; las respuestas de la API nunca exponen el hash.
@Entity
@Table(name = "users")
public class UserEntity {

    // La base de datos asigna un identificador al crear la cuenta.
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // El correo normalizado identifica la cuenta y no puede repetirse.
    @Column(nullable = false, unique = true, length = 254)
    private String email;

    // Guarda únicamente el hash BCrypt, nunca la contraseña original.
    @Column(name = "password_hash", nullable = false, length = 60)
    private String passwordHash;

    // Persiste el rol como texto para que el valor sea legible en la base de datos.
    @Enumerated(EnumType.STRING)
    @Column(name = "global_role", nullable = false, length = 20)
    private GlobalRole globalRole;

    // JPA necesita un constructor sin argumentos para reconstruir cuentas desde filas.
    protected UserEntity() {
    }

    // Construye una cuenta con correo normalizado, hash de contraseña y rol inicial.
    public UserEntity(String email, String passwordHash, GlobalRole globalRole) {
        // Conserva el correo normalizado por el servicio de aplicación.
        this.email = email;
        // Conserva el hash BCrypt generado antes de crear esta entidad.
        this.passwordHash = passwordHash;
        // Conserva el rol que la política de registro ha asignado a la cuenta.
        this.globalRole = globalRole;
    }

    // Devuelve el identificador asignado por la base de datos.
    public Long getId() {
        return id;
    }

    // Devuelve el correo usado como identificador de inicio de sesión.
    public String getEmail() {
        return email;
    }

    // Devuelve el hash que Spring Security compara durante la autenticación.
    public String getPasswordHash() {
        return passwordHash;
    }

    // Devuelve el rol global asociado a la cuenta.
    public GlobalRole getGlobalRole() {
        return globalRole;
    }
}