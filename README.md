# ViajesPlus

Sistema web con API para solicitar, aprobar y auditar reservas de paquetes turísticos. Proyecto del curso Desarrollo de Aplicaciones Empresariales con Scrum y SDD.

**Estado: Sprint 1 terminado** (2 al 6 de noviembre de 2026). Esqueleto técnico (PBI-02) y solicitud de reserva (PBI-03, HU1).

- Entorno de pruebas: https://viajesplus.onrender.com
- API y Swagger: https://viajesplus.onrender.com/swagger-ui.html

## Usuarios de prueba
Contraseña de todos: `Viajes2026`. Proveedor OIDC simulado (IMP-01).

| Rol | Correo |
|---|---|
| Cliente | cliente@viajesplus.co (y cliente2@viajesplus.co) |
| Asesor de viajes | asesor@viajesplus.co |
| Agente de reservas | agente@viajesplus.co |
| Proveedor de hoteles | proveedor@viajesplus.co |
| Coordinador de tours | coordinador@viajesplus.co |
| Guía turístico | guia@viajesplus.co |
| Conductor | conductor@viajesplus.co |
| Contador | contador@viajesplus.co |
| Gerente | gerente@viajesplus.co |
| Administrador | admin@viajesplus.co |

## Dónde está cada cosa
| Qué | Ruta |
|---|---|
| Constitución e instrucciones para la IA | `constitution.md`, `AGENTS.md` |
| Especificación, plan y tareas | `specs/001-viajesplus/spec.md`, `plan.md`, `tasks.md` |
| Decisiones de arquitectura | `docs/adr/` |
| Contrato de la API | `api/openapi.yaml` |
| Working agreement, DoR y DoD | `docs/scrum/working-agreement.md` |
| Clarificación y puertas (preparación) | `docs/scrum/preparacion/` |
| Planning, Daily, Review y Retro del Sprint 1 | `docs/scrum/sprint-1/` |
| Impedimentos | `docs/scrum/impedimentos.md` |
| Bitácora de IA y matriz de trazabilidad | `docs/ia/bitacora.md`, `docs/trazabilidad.md` |
| Código | `src/main/java/co/viajesplus/` (api, application, domain, infrastructure, web) |
| Pruebas | `src/test/java/co/viajesplus/` |

## Ejecutar en local (Spring Tools)
1. `docker compose up -d` (PostgreSQL 16 en el puerto 5432).
2. Importar como proyecto Maven y ejecutar `ViajesPlusApplication`, o `mvn spring-boot:run`.
3. Abrir http://localhost:8080.

Pruebas: `mvn verify` (usa Testcontainers; necesita Docker).

## Desplegar en Render
En Render: **New > Blueprint**, elegir este repositorio y aplicar. El Blueprint (`render.yaml`) crea la base PostgreSQL y el servicio web.
