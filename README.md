# ProjectHub

Backend REST para gestionar proyectos y tareas, construido como portfolio de Java y Spring Boot. El proyecto muestra un flujo vertical real: autenticación, autorización por propietario, reglas de dominio, persistencia relacional, migraciones, pruebas y documentación OpenAPI.

[![Java CI](https://github.com/javiercobasdeveloper/ProjectHub/actions/workflows/ci.yml/badge.svg?branch=main)](https://github.com/javiercobasdeveloper/ProjectHub/actions/workflows/ci.yml)

**English summary:** A modular Spring Boot REST API for project and task management, featuring HTTP Basic authentication, owner-based resource isolation, PostgreSQL, Flyway migrations, OpenAPI, and automated tests.

## Funcionalidades

- Registro de usuarios con correo normalizado y contraseñas almacenadas con BCrypt.
- Autenticación HTTP Basic sin sesión; los endpoints protegidos filtran proyectos y tareas por propietario.
- Gestión de proyectos: crear, listar, consultar, editar y archivar.
- Gestión de tareas: crear, listar, consultar, editar, eliminar y cambiar su estado.
- Los proyectos archivados no admiten nuevas tareas ni modificaciones o eliminaciones de las existentes.
- Reglas de dominio para estados de tareas, validación, prioridades y archivado.
- PostgreSQL con esquema gestionado por cinco migraciones Flyway.
- Contrato OpenAPI y Swagger UI para explorar la API.
- Pruebas unitarias y de integración HTTP; CI con GitHub Actions.

## Tecnologías

Java 21 · Spring Boot 4 · Spring MVC · Spring Security · Spring Data JPA · Hibernate · PostgreSQL 17 · Flyway · Jakarta Validation · OpenAPI/Swagger · JUnit 5 · MockMvc · Maven · Docker Compose · GitHub Actions

## Empezar en local

Requisitos: JDK 21, Maven y Docker con Docker Compose.

```bash
git clone https://github.com/javiercobasdeveloper/ProjectHub.git
cd ProjectHub
docker compose up -d postgres
mvn spring-boot:run
```

La API estará en `http://localhost:8080`. La documentación interactiva está en `http://localhost:8080/swagger-ui/index.html`; el contrato JSON, en `http://localhost:8080/v3/api-docs`.

Compose configura credenciales locales de desarrollo para PostgreSQL. No reutilices esos valores fuera de tu máquina. La aplicación permite sobrescribir la conexión con `DB_URL`, `DB_USERNAME` y `DB_PASSWORD`.

### Probar un flujo

Registra una cuenta de demostración. Usa una contraseña de prueba, no una credencial real:

```bash
curl -i -X POST http://localhost:8080/api/users \
  -H 'Content-Type: application/json' \
  -d '{"email":"demo@example.com","password":"DemoPassword123"}'
```

Crea un proyecto autenticado y usa el ID devuelto para crear una tarea:

```bash
curl -i -X POST http://localhost:8080/api/projects \
  -u demo@example.com:DemoPassword123 \
  -H 'Content-Type: application/json' \
  -d '{"name":"Proyecto de demostración"}'

curl -i -X POST http://localhost:8080/api/projects/1/tasks \
  -u demo@example.com:DemoPassword123 \
  -H 'Content-Type: application/json' \
  -d '{"title":"Preparar la demo","description":"Primer flujo","priority":"HIGH"}'
```

Si la base ya contiene proyectos, sustituye `1` por el identificador real de la respuesta anterior. Para cerrar la demo, detén la base con `docker compose down`; los datos permanecen en el volumen local.

## Pruebas

```bash
mvn verify
```

Las pruebas automatizadas usan H2 y no necesitan PostgreSQL ni Docker. Última verificación local registrada: 40 pruebas, sin fallos ni errores. El workflow de GitHub Actions ejecuta `mvn verify` con Java 21.

## Seguridad y alcance

Salud, registro y documentación OpenAPI son públicos. Las operaciones de proyectos y tareas requieren autenticación y solo permiten acceder a recursos propios; los recursos ajenos responden `404`. HTTP Basic debe utilizarse únicamente sobre HTTPS en cualquier entorno desplegado. La configuración actual es un MVP educativo, no una auditoría de seguridad ni una configuración de producción.

Aún no están implementados la administración de usuarios, la autorización avanzada por roles, los miembros de proyecto, la reasignación de tareas, los comentarios ni un despliegue público. Las cuentas registradas reciben el rol `MEMBER`; durante esta fase ese rol puede crear sus propios proyectos.

## Documentación

- [Contrato y endpoints de la API](ProjectHub-documentacion/ProjectHub-docs/docs/API.md)
- [Arquitectura](ProjectHub-documentacion/ProjectHub-docs/docs/ARCHITECTURE.md)
- [Modelo de dominio](ProjectHub-documentacion/ProjectHub-docs/docs/DOMAIN-MODEL.md)
- [Roadmap](ProjectHub-documentacion/ProjectHub-docs/docs/ROADMAP.md)
- [Decisiones de arquitectura (ADR)](ProjectHub-documentacion/ProjectHub-docs/docs/adr/)
