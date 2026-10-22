# ADR 0004 — Autenticación y propiedad de recursos

## Estado

Aceptada

## Fecha

2026-10-21

## Decisión

- Usar HTTP Basic sin sesión para la API REST.
- Identificar las cuentas mediante correo normalizado y almacenar contraseñas con BCrypt.
- Permitir el registro público; el servidor asigna `MEMBER` y no acepta roles desde el cliente.
- Asociar cada proyecto con el usuario que lo crea y filtrar las tareas mediante el propietario del proyecto.
- Devolver `404 Not Found` tanto para recursos inexistentes como para recursos ajenos.
- Durante esta fase, permitir que `MEMBER` cree proyectos propios para completar el flujo de propiedad. La matriz global de permisos y la provisión segura de cuentas privilegiadas quedan aplazadas.
- Mantener proyectos existentes sin propietario fuera de las consultas autenticadas; no asignar propietarios ficticios durante la migración.

## Motivo

HTTP Basic permite demostrar autenticación Spring Security de forma pequeña en una API sin frontend. El filtro por propietario completa un flujo vertical verificable sin anticipar la relación de miembros ni el sistema de roles completo. La respuesta 404 evita revelar la existencia de recursos de otra cuenta.

## Consecuencias

- El endpoint de salud y el registro son públicos; los demás requieren autenticación.
- HTTP Basic debe usarse bajo HTTPS en despliegues reales.
- El modelo actual usa `Project.owner`; la futura relación `ProjectMember` podrá sustituirlo o ampliarlo cuando se implemente autorización por roles.
- La regla global de creación por rol queda temporalmente distinta del requisito final: los usuarios registrados son `MEMBER`, pero pueden crear proyectos propios.