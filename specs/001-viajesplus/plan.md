# Plan técnico: ViajesPlus · Versión 1.0

Ruta: `specs/001-viajesplus/plan.md` · Tarjeta D06 · Redacta: Meryi Yimet Bueno Mantilla (Dev Spec)
Aprobado en la Puerta 2 el 29 de octubre de 2026 · Base: spec v1.0

## 1. Decisiones técnicas

| Aspecto | Decisión | Justificación |
|---|---|---|
| Lenguaje y framework | Java 21 y Spring Boot 3 | Estándar en aplicaciones empresariales; el equipo lo conoce |
| Base de datos | PostgreSQL 16 y Flyway | Transacciones y restricciones CHECK para el cupo (REQ-VIA-005, ADR-002) |
| Seguridad | Spring Security como servidor de recursos OIDC (JWT) | NFR-VIA-002; ver ADR-003 e IMP-01 |
| Pruebas | JUnit 5 y Testcontainers | Pruebas de integración con la base real |
| Contrato | OpenAPI 3 (`api/openapi.yaml`) | Contrato primero; prueba de contrato en CI |
| Arquitectura | Monolito modular con capas hexagonales | Equipo de dos personas y un solo despliegue (ADR-001) |
| Pantallas | Thymeleaf con Bootstrap, servidas por la misma aplicación | Pantallas sencillas sin un segundo despliegue |
| Despliegue de pruebas | Render (Docker) con PostgreSQL administrado | Gratuito para el curso; se despliega en cada push a main |

## 2. Estructura de paquetes (la crea T001)

```
src/main/java/co/viajesplus/
├── api/              # controladores REST, DTO y manejo de errores (422, 403, 404)
├── application/      # casos de uso: CatalogService, ReservationService, AuditService
├── domain/           # reglas puras: QuoteCalculator, ReservationStatus, Role
├── infrastructure/   # JPA y repositorios, seguridad OIDC, configuración
└── web/              # pantallas Thymeleaf
```

## 3. Modelo de datos (migración V1, T003)

| Tabla | Para qué | Restricciones clave |
|---|---|---|
| travel_package | Paquetes con tarifa por persona | price_per_person > 0 |
| departure | Salidas con cupo | `ck_departure_cupo`: 0 ≤ occupied_seats ≤ capacity (RN-03) |
| reservation | Reservas | status en PENDIENTE, CONFIRMADA, RECHAZADA, CANCELADA; travelers > 0 |
| audit_log | Auditoría de cambios de estado | Solo inserción (REQ-VIA-011) |
| app_user | Usuarios del proveedor OIDC simulado | Se retira al conectar el tenant real (D21) |

## 4. Contrato de la API

| Método y ruta | Rol | Cubre | Sprint |
|---|---|---|---|
| POST /api/auth/token | Público (OIDC simulado) | NFR-VIA-002 | S1 |
| GET /api/packages, /api/packages/{id} | Público | Catálogo | S1 |
| GET /api/packages/{id}/quote | Cliente, Asesor, Agente | REQ-VIA-001, 002 | S1 |
| POST /api/reservations | Cliente | REQ-VIA-003 | S1 |
| GET /api/reservations, /api/reservations/{id} | Autenticado (el cliente solo las suyas) | REQ-VIA-012 | S1 |
| GET /api/audit | Gerente, Administrador | REQ-VIA-011 | S1 |
| POST /api/reservations/{id}/confirm y /reject | Agente | REQ-VIA-004 a 008 | S2 |
| /api/reservations/{id}/assignment | Coordinador | REQ-VIA-009 | S2 |
| POST /api/reservations/{id}/cancel | Cliente | REQ-VIA-010 | S3 |

Errores de negocio: 422 con `{ "code", "message", "details" }`. Códigos: DATOS_INVALIDOS (S1), CUPO_INSUFICIENTE, ANTELACION_INSUFICIENTE, SIN_DISPONIBILIDAD, MOTIVO_OBLIGATORIO (S2). Recurso ajeno o rol sin permiso: 403 ACCESO_DENEGADO. Sin token: 401.

Las reglas de autorización de los sprints 2 y 3 se declaran desde el Sprint 1 en `SecurityConfig`: un rol sin permiso recibe 403 aunque el endpoint todavía no exista.

## 5. Matriz requisito → componente (Puerta 2)

| Requisito | Componente o decisión |
|---|---|
| REQ-VIA-001 y 002 | QuoteCalculator (dominio) y CatalogService |
| REQ-VIA-003 | ReservationService.request |
| REQ-VIA-004, 006, 007 y 008 | DecideReservation (S2) |
| REQ-VIA-005 | DepartureRepository.occupySeats y restricción ck_departure_cupo (ADR-002) |
| REQ-VIA-009 | AssignHotelAndTour (S2) |
| REQ-VIA-010 | CancelReservation (S3) |
| REQ-VIA-011 | AuditService sobre audit_log |
| REQ-VIA-012 | SecurityConfig (OIDC y roles) y ReservationService.get |
| NFR-VIA-001 a 004 | Prueba k6 (T017), seguridad (T004), notificaciones (ADR-004) y política de datos (D15) |

## 6. Ajuste por IMP-01 (5 de noviembre de 2026)

No hay proveedor OIDC de pruebas. Mientras TI entrega el tenant, la aplicación incluye un proveedor simulado (`SimulatedOidcProvider`) que emite tokens JWT RS256 con los mismos claims (`sub`, `name`, `roles`). Con la variable `VIAJESPLUS_OIDC_ISSUER_URI` la aplicación valida los tokens del proveedor real sin cambiar el resto del código. Revisado contra la constitución (principio 4): el simulador solo existe en pruebas y no guarda datos reales.
