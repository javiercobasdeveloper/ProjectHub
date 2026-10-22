# ADR 0001 — Arquitectura inicial

## Estado

Aceptada

## Decisión

ProjectHub será un **monolito modular** en lugar de microservicios.

## Motivo

El objetivo es demostrar buenas prácticas de backend Java y mantener el proyecto razonablemente pequeño y explicable.

Un único backend permite:

- menor complejidad operativa
- despliegue más sencillo
- menor coste
- desarrollo más apropiado para un proyecto individual
- separación modular sin introducir distribución innecesaria

## Consecuencia

La aplicación debe estar modularizada internamente para que pueda evolucionar sin convertirse en un bloque desordenado.
