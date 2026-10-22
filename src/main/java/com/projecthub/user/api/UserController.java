package com.projecthub.user.api;

// Importa el servicio que coordina el registro de cuentas.
import com.projecthub.user.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.net.URI;

// Expone el registro público de cuentas nuevas.
@RestController
@RequestMapping("/api/users")
@Tag(name = "Usuarios", description = "Registro de cuentas")
public class UserController {

    // Servicio de aplicación que valida y persiste las cuentas registradas.
    private final UserService userService;

    // Recibe el servicio mediante inyección de dependencias de Spring.
    public UserController(UserService userService) {
        // Conserva el servicio para delegar las peticiones de registro.
        this.userService = userService;
    }

    // Registra una cuenta y devuelve HTTP 201 sin filtrar la contraseña.
    @PostMapping
    @Operation(summary = "Registrar usuario", description = "Crea una cuenta pública con rol MEMBER y contraseña almacenada como hash BCrypt.")
        @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Cuenta creada"),
            @ApiResponse(responseCode = "400", description = "El correo o la contraseña no son válidos"),
            @ApiResponse(responseCode = "409", description = "El correo ya está registrado")
        })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterUserRequest request) {
        // Ejecuta el alta con datos que ya superaron las validaciones del DTO.
        UserResponse response = userService.register(request.email(), request.password());
        // Construye la ubicación del recurso nuevo para el cliente.
        URI location = URI.create("/api/users/" + response.id());
        // Devuelve Created junto con los datos públicos de la cuenta.
        return ResponseEntity.created(location).body(response);
    }
}