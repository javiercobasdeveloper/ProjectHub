# ADR 0002 — Reglas de dominio del MVP

## Estado

Aceptada

## Decisiones

- Los proyectos se archivan en lugar de eliminarse físicamente.
- Cada tarea tiene un único responsable.
- Los estados son TODO, IN_PROGRESS y DONE.
- Las únicas transiciones son TODO->IN_PROGRESS, IN_PROGRESS->TODO e IN_PROGRESS->DONE.
- Los proyectos archivados no permiten operaciones normales.
- Se usan IDs Long.
- El historial de auditoría queda fuera del MVP.

## Motivo

Estas reglas permiten tener un dominio suficientemente realista para practicar autorización y lógica de negocio sin aumentar innecesariamente el alcance.
