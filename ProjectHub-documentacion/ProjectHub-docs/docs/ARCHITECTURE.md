# Arquitectura — ProjectHub

## Estilo

Monolito modular.

El objetivo es tener un único backend desplegable, pero dividido internamente en módulos con responsabilidades claras.

## Módulos previstos

```text
com.projecthub
├── user
├── project
├── task
├── comment
├── security
├── common
└── config
```

## Capas

### Controller

Recibe HTTP, valida entrada básica y delega.

No debe contener reglas de negocio complejas.

### Service / Application

Coordina casos de uso y aplica reglas de negocio.

Aquí deben vivir decisiones como:

- si un usuario tiene permiso
- si un proyecto está archivado
- si una transición de tarea es válida
- si una persona puede asignar una tarea

### Repository

Acceso a persistencia.

Cuando llegue JPA, las interfaces de repositorio se encargarán de las consultas necesarias.

### Domain / Entity

Modelo del sistema y reglas que pertenezcan realmente a las entidades.

No convertir las entidades en simples bolsas de datos si una regla pertenece de forma natural al dominio.

### DTO

Objetos destinados a entrada/salida de API.

Evitar exponer entidades JPA directamente sin una razón clara.

### Exception

Excepciones propias y gestión centralizada de errores HTTP.

## Persistencia

PostgreSQL + JPA/Hibernate.

No se introducirá base de datos durante la fase de Java puro.

## Seguridad

Spring Security autentica la API mediante HTTP Basic stateless. Las cuentas se persisten en el módulo `user`; el correo normalizado es el identificador de acceso y las contraseñas se almacenan mediante BCrypt. El registro es público y asigna siempre `MEMBER`.

La cadena de seguridad deja públicos el endpoint de salud y el registro, y exige autenticación para el resto de rutas. La autorización de propiedad se ejecuta en servicios/repositorios: los proyectos se filtran por su propietario y las tareas por el propietario de su proyecto. Los recursos ausentes y ajenos se presentan como `404`.

En este incremento, el propietario se almacena directamente en `ProjectEntity`; `ProjectMember`, roles de proyecto y la matriz global de permisos quedan pendientes. Temporalmente, cualquier cuenta `MEMBER` puede crear sus propios proyectos. HTTP Basic debe desplegarse únicamente bajo HTTPS.

Las tareas guardan prioridad y responsable. La prioridad por defecto es `MEDIUM`; el responsable inicial es el propietario del proyecto. La edición básica se coordina en los servicios y filtra primero por propietario. No se pueden crear, editar ni borrar tareas en proyectos archivados; PATCH no cambia el estado ni permite reasignar.

## API

REST documentada automáticamente con Springdoc OpenAPI 3.1.1, compatible con Spring Boot 4. El contrato JSON se sirve en `/v3/api-docs` y Swagger UI en `/swagger-ui/index.html`. La especificación declara HTTP Basic para proyectos y tareas; salud, registro y documentación permanecen públicos.

## Configuración

La configuración sensible no debe estar en Git.

Utilizar variables de entorno y archivos de ejemplo sin secretos reales.

## Evolución

La arquitectura debe ser suficiente para crecer, pero no se diseñará para microservicios ni sistemas distribuidos que el proyecto todavía no necesita.
