# ViajesPlus

Sistema web con API para solicitar, aprobar y auditar reservas de paquetes turísticos. Proyecto del curso Desarrollo de Aplicaciones Empresariales con Scrum y SDD.

**Estado: Sprint 2 terminado** (9 al 13 de noviembre de 2026).
- Sprint 1: esqueleto técnico (PBI-02) y solicitud de reserva (PBI-03, HU1).
- Sprint 2: el agente confirma o rechaza (PBI-04, HU2) y el coordinador asigna hotel, guía y conductor (PBI-05, HU3).

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
| Guía turístico | guia@viajesplus.co (y guia2@viajesplus.co) |
| Conductor | conductor@viajesplus.co (y conductor2@viajesplus.co) |
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
| Planning, Daily, Review y Retro | `docs/scrum/sprint-1/` y `docs/scrum/sprint-2/` |
| Impedimentos | `docs/scrum/impedimentos.md` |
| Bitácora de IA y matriz de trazabilidad | `docs/ia/bitacora.md`, `docs/trazabilidad.md` |
| Código | `src/main/java/co/viajesplus/` (api, application, domain, infrastructure, web) |
| Pruebas | `src/test/java/co/viajesplus/` |

## Qué ve cada rol
| Rol | Pantalla |
|---|---|
| Cliente | Paquetes, cotizar, solicitar reserva, Mis reservas y Avisos |
| Asesor | Cotizar para un cliente |
| Agente | Bandeja: confirmar o rechazar con motivo |
| Proveedor | Disponibilidad y tarifa de sus hoteles por salida |
| Coordinador | Asignar hotel, guía y conductor |
| Guía y conductor | Salidas asignadas y avisos |
| Gerente y administrador | Auditoría |

## Ejecutar en local (Spring Tools)
1. `docker compose up -d` (PostgreSQL 16 en el puerto 5432).
2. Importar como proyecto Maven y ejecutar `ViajesPlusApplication`, o `mvn spring-boot:run`.
3. Abrir http://localhost:8080 (la aplicación usa siempre el puerto 8080).

Pruebas: `mvn verify` (usa Testcontainers; necesita Docker).

## Desplegar en Render
En Render: **New > Blueprint**, elegir este repositorio y aplicar. El Blueprint (`render.yaml`) crea la base PostgreSQL y el servicio web.
