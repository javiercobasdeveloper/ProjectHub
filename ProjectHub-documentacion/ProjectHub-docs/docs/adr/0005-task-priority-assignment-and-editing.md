# ADR 0005 — Prioridad, responsable y edición básica de tareas

## Estado

Aceptada

## Fecha

2026-10-21

## Decisión

- Las prioridades son `LOW`, `MEDIUM`, `HIGH` y `URGENT`; una tarea nueva usa `MEDIUM` si el cliente no indica otra.
- El responsable inicial de una tarea es el propietario de su proyecto.
- La reasignación queda pendiente hasta implementar miembros de proyecto.
- `PATCH /api/projects/{id}` actualiza el nombre de un proyecto propio activo.
- `PATCH /api/tasks/{id}` actualiza título, descripción o prioridad de una tarea propia, sin cambiar estado ni responsable.
- `DELETE /api/tasks/{id}` elimina una tarea propia de un proyecto activo y responde `204`.
- En PATCH, los campos omitidos conservan el valor actual; una descripción vacía elimina su texto.
- Los proyectos archivados y sus tareas no admiten edición normal.

## Motivo

El default `MEDIUM` y la asignación al propietario producen tareas completas sin requerir una matriz de roles ni miembros que todavía no existen. La edición básica permite demostrar el ciclo de vida de recursos en la demo sin añadir eliminación ni reglas de reasignación.

## Consecuencias

- `Task` incluye prioridad y descripción; la respuesta API expone también `assigneeId`.
- La migración existente rellena prioridad con `MEDIUM` y asigna tareas existentes al propietario del proyecto cuando está disponible.
- El borrado de tareas se limita al propietario y a proyectos activos.
- Las tareas históricas cuyo proyecto carece de propietario permanecen sin responsable; los nuevos proyectos y tareas quedan asociados a un usuario.