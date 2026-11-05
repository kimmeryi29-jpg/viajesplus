# Tareas: ViajesPlus (spec v1.0)            [P] = puede ir en paralelo

Ruta: `specs/001-viajesplus/tasks.md` · Tarjeta D07 · Redacta: Meryi Yimet Bueno Mantilla (Dev QA)
Aprobado en la Puerta 3 el 29 de octubre de 2026. Cada línea es una tarjeta del tablero.

## Fase 0 · Preparación (PBI-02)
- [x] T001 Proyecto Spring Boot 3 + Java 21, build y CI (Constitución 3 y 7, ADR-001)
- [x] T002 PostgreSQL 16, Flyway y Testcontainers (después de T001; ADR-001, ADR-002)

## Fase 1 · Fundamentos (PBI-02; bloquea las historias)
- [x] T003 Migración V1 y restricción de cupo (REQ-VIA-005, 011, RN-03, ADR-002)
- [x] T004 Seguridad OIDC y 10 roles (REQ-VIA-012, NFR-VIA-002, Constitución 4)
- [x] T005 AuditService sobre la tabla audit_log (REQ-VIA-011, AC-011.1)

## Fase 2 · HU1 Solicitar una reserva (PBI-03)
- [x] T006 [P] [US1] Pruebas de QuoteCalculator (AC-002.1 a 002.3)
- [x] T007 [US1] Implementar QuoteCalculator (REQ-VIA-002, RN-01)
- [x] T008 [US1] ReservationService y endpoints (REQ-VIA-001, 003, AC-001.1, 003.1)

## Fase 3 · HU2 Confirmar o rechazar (PBI-04)
- [ ] T009 [P] [US2] Pruebas de confirmación (14) (AC-004.x a 008.x)
- [ ] T010 [US2] Caso de uso DecideReservation (REQ-VIA-004 a 008, RN-02 a 05)
- [ ] T011 [US2] POST confirm/reject y OpenAPI (códigos 422 y 403)
- [ ] T012 [P] [US2] Notificación al cliente (NFR-VIA-003, ADR-004)

## Fase 4 · HU3 Asignar hotel y tour (PBI-05)
- [ ] T013 [P] [US3] Pruebas de asignación y cruces (AC-009.1 a 009.3)
- [ ] T014 [US3] Caso de uso AssignHotelAndTour (REQ-VIA-009, RN-05, 06)
- [ ] T015 [US3] Endpoints de asignación y avisos

## Fase 5 · HU4 Cancelar (PBI-06)
- [ ] T016 [US4] CancelReservation y endpoint (REQ-VIA-010, AC-010.1, RN-07)

## Fase final (PBI-07)
- [ ] T017 [P] Prueba de carga k6 de la cotización (NFR-VIA-001)
- [ ] T018 Matriz de trazabilidad final

## Objetivo extendido · HU5 (PBI-08, no se inicia)
- [ ] T024 [P] [US5] Pruebas AC-014.x y AC-015.1
- [ ] T025 [US5] Consulta TourItineraryQuery (REQ-VIA-014)
- [ ] T026 [US5] GET /tours/{id}/itinerary y OpenAPI
- [ ] T027 [US5] Autorización por rol (REQ-VIA-015)
- [ ] T028 [US5] Actualizar matriz de trazabilidad

Punto de control tras la Fase 2: demostrar la solicitud de una reserva de punta a punta (Review del Sprint 1).

Las tarjetas se crean justo a tiempo: T001 a T008 en el Sprint 1, T009 a T015 en el Sprint 2 y T016 a T018 en el Sprint 3. Las tareas T019 a T023 nacerán del delta de CR-001.
