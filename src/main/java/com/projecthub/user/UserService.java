package com.projecthub.user;

// Importa DTOs y persistencia usados por los casos de uso de cuentas.
import com.projecthub.user.api.UserResponse;
import com.projecthub.user.persistence.UserEntity;
import com.projecthub.user.persistence.UserRepository;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

// Coordina el registro de cuentas y adapta usuarios persistidos a Spring Security.
@Service
public class UserService implements UserDetailsService {

    // Repositorio que consulta y guarda las cuentas de usuario.
    private final UserRepository userRepository;
    // Codificador que transforma contraseñas en hashes BCrypt no reversibles.
    private final PasswordEncoder passwordEncoder;

    // Recibe las dependencias necesarias para registrar y autenticar cuentas.
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        // Conserva el repositorio para consultar y persistir usuarios.
        this.userRepository = userRepository;
        // Conserva el codificador para no guardar contraseñas en texto claro.
        this.passwordEncoder = passwordEncoder;
    }

    // Registra una cuenta pública con rol MEMBER y devuelve solo datos seguros.
    @Transactional
    public UserResponse register(String email, String password) {
        // Normaliza el correo para evitar cuentas duplicadas por mayúsculas o espacios.
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        // Rechaza un correo que ya pertenezca a una cuenta existente.
        if (userRepository.existsByEmailIgnoreCase(normalizedEmail)) {
            throw new EmailAlreadyRegisteredException(normalizedEmail);
        }
        // Codifica la contraseña antes de crear la entidad que se persistirá.
        String passwordHash = passwordEncoder.encode(password);
        // Asigna MEMBER desde el servidor para impedir que el cliente elija privilegios.
        UserEntity user = new UserEntity(normalizedEmail, passwordHash, GlobalRole.MEMBER);
        // Guarda la cuenta para obtener su identificador persistido.
        UserEntity savedUser = userRepository.save(user);
        // Devuelve un DTO que omite la credencial almacenada.
        return toResponse(savedUser);
    }

    // Carga la cuenta por correo para que Spring Security valide la contraseña presentada.
    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Normaliza el identificador de acceso igual que durante el registro.
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);
        // Busca la cuenta y convierte su ausencia al error esperado por Spring Security.
        UserEntity user = userRepository.findByEmailIgnoreCase(normalizedEmail)
                .orElseThrow(() -> new UsernameNotFoundException("No existe la cuenta solicitada"));
        // Adapta el usuario persistido al contrato que usa el proveedor de autenticación.
        return User.withUsername(user.getEmail())
                .password(user.getPasswordHash())
                .roles(user.getGlobalRole().name())
                .build();
    }

    // Convierte una cuenta interna en la representación pública sin credenciales.
    private UserResponse toResponse(UserEntity user) {
        // Expone solo identificador, correo y rol global.
        return new UserResponse(user.getId(), user.getEmail(), user.getGlobalRole());
    }
}