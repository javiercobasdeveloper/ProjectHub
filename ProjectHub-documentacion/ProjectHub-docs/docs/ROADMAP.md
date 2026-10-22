# Roadmap — ProjectHub

```text
PREPARACIÓN Y DOMINIO JAVA                 COMPLETADOS
                    ↓
API REST + JPA + MIGRACIONES               IMPLEMENTADOS
PostgreSQL real                            VALIDADO LOCALMENTE
                    ↓
AUTENTICACIÓN + PROPIEDAD DE RECURSOS      IMPLEMENTADOS*
                    ↓
OPENAPI + CAMPOS + CRUD BÁSICO             IMPLEMENTADOS
                    ↓
POSTGRESQL + DEMO LOCAL                    IMPLEMENTADOS*
CI EN GITHUB                               PENDIENTE DE CONFIRMAR
                    ↓
V2: ROLES AVANZADOS, COMENTARIOS, ETC.     FUTURO
```

Los tests acompañan todas las fases; no son una fase aislada. La aplicación de consola queda como práctica opcional y no retrasa la API web.

* La fase de seguridad usa HTTP Basic y propiedad de recurso. La matriz global de roles y el alta de cuentas privilegiadas siguen pendientes; por ahora, `MEMBER` puede crear proyectos propios.
* PostgreSQL 17.11, las migraciones y el flujo local están verificados. El workflow existe y `mvn verify` pasa localmente; falta confirmar el resultado del workflow alojado en GitHub.
