# Modelo de dominio — ProjectHub

## User

Representa a una persona que utiliza el sistema.

Campos persistidos en la implementación actual:

- id: Long
- email normalizado y único, utilizado como identificador de acceso
- hash BCrypt de la contraseña (nunca se devuelve por API)
- globalRole: ADMIN | MANAGER | MEMBER

El registro asigna el rol `MEMBER`; el cliente no puede elegir el rol.
Los campos `active`, `createdAt` y `updatedAt` son parte del diseño futuro y todavía no existen en la entidad persistente.

## Project

Representa un proyecto.

Campos persistidos en la implementación actual:

- id: Long
- name: 1-100
- status: ACTIVE | ARCHIVED
- owner: User

En el MVP actual, `owner` se guarda directamente en el proyecto para aislar recursos. La relación `ProjectMember` y los roles de proyecto siguen pendientes; cuando se implementen, el creador se representará como OWNER mediante esa relación.
La descripción del proyecto y las fechas `createdAt`/`updatedAt` son futuras; no están implementadas en la entidad ni en la API.

## ProjectMember

Representa la pertenencia de un usuario a un proyecto.

Campos conceptuales:

- id: Long
- project
- user
- projectRole: OWNER | MANAGER | MEMBER
- joinedAt

Debe existir una única pertenencia por combinación proyecto + usuario.

## Task

Representa trabajo pendiente dentro de un proyecto.

Campos persistidos en la implementación actual:

- id: Long
- project
- title: 1-150
- description: opcional, máximo 2000 (implementado)
- status: TODO | IN_PROGRESS | DONE
- priority: LOW | MEDIUM | HIGH | URGENT (por defecto MEDIUM)
- assignee: User (por defecto, propietario del proyecto)

La tarea tiene un responsable inicial, que es el propietario del proyecto. La reasignación no está implementada. `createdBy` y las fechas `createdAt`/`updatedAt` son futuras; no están en la entidad persistente actual.

## Comment

Representa una conversación asociada a una tarea.

Campos:

- id: Long
- task
- author: User
- content: 1-2000
- createdAt
- updatedAt

## Relaciones implementadas

```text
User 1 --- N Project (owner)
Project 1 --- N Task
Task N --- 1 User (assignee)
```

## Relaciones previstas

```text
User 1 --- N ProjectMember N --- 1 Project
Project 1 --- N Task
Task N --- 1 User (assignee)
Task 1 --- N Comment
Comment N --- 1 User (author)
```

`ProjectMember`, `Comment` y las relaciones de autoría mostradas arriba son diseño futuro; todavía no existen en el código ni en las migraciones.

## Decisiones

- IDs Long en MVP.
- Sin borrado físico de proyectos.
- Auditoría completa fuera del MVP.
- Una sola persona responsable por tarea.
- La propiedad de tareas se hereda del proyecto al que pertenecen.
- La descripción de tarea es opcional y admite hasta 2000 caracteres.
- La prioridad inicial es `MEDIUM`; la creación puede indicar otro valor admitido.
- El responsable inicial es el propietario del proyecto; no se permite reasignar hasta implementar miembros.
- PATCH conserva los campos omitidos; la descripción se borra enviando cadena vacía.
- Durante la fase de propiedad, `MEMBER` puede crear proyectos propios. La restricción global de creación por rol se aplaza hasta implementar el flujo de provisión de cuentas privilegiadas.
