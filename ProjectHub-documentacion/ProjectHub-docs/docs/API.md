# API — ProjectHub

La API se implementa progresivamente. Los endpoints indicados como pendientes son el mapa de trabajo, no funcionalidades ya disponibles.

## Implementado

### Salud

```text
GET /api/health
```

Devuelve `{"status":"UP"}`.

### Usuarios

```text
POST /api/users
```

Registro público con `email` y `password` (8-72 caracteres). El correo se normaliza a minúsculas y es único. El servidor asigna el rol `MEMBER`; el cliente no puede elegirlo. La respuesta es `201 Created` y no incluye la contraseña ni su hash. Un correo duplicado devuelve `409 Conflict`.

### Proyectos

```text
POST /api/projects
GET  /api/projects
GET  /api/projects/{id}
PATCH /api/projects/{id}
POST /api/projects/{id}/archive
```

La creación recibe un JSON con `name` obligatorio, no compuesto solo por espacios y con un máximo de 100 caracteres. Devuelve `201 Created` y una cabecera `Location`. Un identificador inexistente devuelve `404`; un cuerpo no válido devuelve `400`.

Los proyectos se persisten en PostgreSQL y se archivan sin borrado físico.

Los endpoints de proyectos y tareas requieren autenticación HTTP Basic. Cada proyecto queda asociado a la cuenta autenticada que lo crea. Los listados solo muestran recursos propios; acceder a un proyecto o tarea ajenos devuelve `404 Not Found` para no revelar su existencia. En esta fase, un usuario `MEMBER` puede crear proyectos propios; la restricción global por rol queda aplazada hasta completar la autorización por roles.

### Tareas

```text
POST /api/projects/{projectId}/tasks
GET  /api/projects/{projectId}/tasks
GET  /api/tasks/{id}
PATCH /api/tasks/{id}
DELETE /api/tasks/{id}
POST /api/tasks/{id}/start
POST /api/tasks/{id}/complete
POST /api/tasks/{id}/return-to-todo
```

La creación recibe `title` obligatorio de 1 a 150 caracteres, `description` opcional de hasta 2000 caracteres y `priority` opcional (`LOW`, `MEDIUM`, `HIGH`, `URGENT`), cuyo valor por defecto es `MEDIUM`. Requiere un proyecto existente propiedad del usuario autenticado. La tarea se asigna inicialmente al propietario del proyecto. Las respuestas incluyen descripción, prioridad y `assigneeId`. Los estados permitidos son `TODO`, `IN_PROGRESS` y `DONE`; una transición no permitida devuelve `409 Conflict`. La tarea se persiste asociada a su proyecto.

`PATCH /api/projects/{id}` recibe el nuevo `name`. `PATCH /api/tasks/{id}` permite enviar `title`, `description` o `priority`; los campos omitidos conservan su valor. Para borrar la descripción se envía una cadena vacía. Ninguna edición cambia estado o responsable y los recursos de proyectos archivados no se pueden editar.

No se pueden crear tareas nuevas dentro de proyectos archivados. El intento devuelve `409 Conflict`, igual que las operaciones de edición o borrado que no están permitidas por el estado del proyecto.

## Pendiente

### Usuarios

```text
GET    /api/users
GET    /api/users/{id}
PATCH  /api/users/{id}
```

El registro `POST /api/users` está implementado; la consulta y administración de usuarios siguen pendientes.

### Miembros

```text
GET    /api/projects/{projectId}/members
POST   /api/projects/{projectId}/members
PATCH  /api/projects/{projectId}/members/{userId}
DELETE /api/projects/{projectId}/members/{userId}
```

La reasignación de tareas, administración de usuarios, miembros, comentarios y autorización por roles siguen pendientes.

### Comentarios

```text
GET    /api/tasks/{taskId}/comments
POST   /api/tasks/{taskId}/comments
DELETE /api/comments/{id}
```

## Reglas para la API

- Utilizar nombres consistentes.
- Validar DTOs de entrada.
- Utilizar códigos HTTP adecuados.
- No devolver información sensible.
- Errores con formato consistente.
- El contrato OpenAPI está disponible en `GET /v3/api-docs` y Swagger UI en `/swagger-ui/index.html`.
- Swagger UI y el spec son públicos; las operaciones de proyectos/tareas documentan y requieren HTTP Basic.
- Documentar autenticación y permisos en cada operación protegida.

## Importante

La autenticación HTTP Basic es stateless y requiere enviar las credenciales en cada petición protegida. El endpoint de salud, el registro y la documentación son públicos. En despliegues reales, HTTP Basic debe usarse únicamente bajo HTTPS.
