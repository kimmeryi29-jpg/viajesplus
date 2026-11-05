# Matriz de trazabilidad · Sprint 1

Requisito → criterio → prueba → resultado. Se completa en cada sprint (T018 cierra la matriz en el Sprint 3).

| Requisito | Criterio | Prueba automatizada | Tarea | Resultado (6/11/2026) |
|---|---|---|---|---|
| REQ-VIA-001 | AC-001.1 | ReservationApiIT.cotizacionCliente, cotizacionAsesor | T008 | Pasa |
| REQ-VIA-002 | AC-002.1 | QuoteCalculatorTest.tresViajeros | T006, T007 | Pasa |
| REQ-VIA-002 | AC-002.2 | QuoteCalculatorTest.unViajero | T006, T007 | Pasa |
| REQ-VIA-002 | AC-002.3 | QuoteCalculatorTest.ceroViajeros | T006, T007 | Pasa |
| REQ-VIA-003 | AC-003.1 | ReservationApiIT.solicitudValidaYAuditada | T008 | Pasa |
| REQ-VIA-003 | AC-003.2 | ReservationApiIT.sinFecha, ceroViajeros, fechaSinSalida | T008 | Pasa |
| REQ-VIA-005 | AC-005.1 | DepartureCapacityIT.ultimaPlazaSimultanea | T003 | Pasa |
| REQ-VIA-005 | AC-005.2 | DepartureCapacityIT.restriccionEnLaBase | T003 | Pasa |
| REQ-VIA-011 | AC-011.1 | ReservationApiIT.solicitudValidaYAuditada | T005 | Pasa |
| REQ-VIA-012 | AC-012.1 | ReservationApiIT.reservaAjena | T004, T008 | Pasa |
| REQ-VIA-012 | AC-012.2 | ReservationApiIT.clienteNoConfirma, clienteSinAuditoria | T004 | Pasa |
| REQ-VIA-012 | AC-012.3 | ReservationApiIT.credencialesInvalidas, WebIT.loginWeb | T004 | Pasa |
| Contrato | DoD 9 | ContractTest.contratoIgualALaApi | T008 | Pasa |
| REQ-VIA-004, 006 a 010 | AC-004.x, 006.x a 010.x | Sprints 2 y 3 | T009 a T016 | Pendiente |

Cobertura del Sprint 1: los 6 requisitos comprometidos (001, 002, 003, 005, 011 y 012) tienen criterio y prueba.
