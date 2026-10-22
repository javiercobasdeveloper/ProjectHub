package com.projecthub;

// Importa la anotación que activa el escaneo de componentes y la configuración automática de Spring Boot.
import org.springframework.boot.autoconfigure.SpringBootApplication;
// Importa el punto de entrada que arranca el contexto de Spring Boot.
import org.springframework.boot.SpringApplication;

// Marca la clase principal de la aplicación web ProjectHub.
@SpringBootApplication
public class ProjectHubApplication {

    // Arranca la aplicación y deja que Spring configure sus componentes.
    public static void main(String[] args) {
        // Inicia el servidor web y carga los controladores encontrados bajo com.projecthub.
        SpringApplication.run(ProjectHubApplication.class, args);
    }
}