# Matriz de trazabilidad · Sprints 1 y 2

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
| REQ-VIA-004 | AC-004.1 | DecisionApiIT.sinCupo | T009, T010 | Pasa (13/11) |
| REQ-VIA-004 | AC-004.2 | DecisionApiIT.cupoExacto | T009, T010 | Pasa (13/11) |
| REQ-VIA-004 | AC-004.3 | DecisionApiIT.pendientesOcupanCupo | D09, T010 | Pasa (13/11) |
| REQ-VIA-005 | AC-005.1 | DecisionApiIT.ultimaPlazaSimultanea | T009, T010 | Pasa (13/11) |
| REQ-VIA-006 | AC-006.1 | DecisionApiIT.cuatroDias | T009, T010 | Pasa (13/11) |
| REQ-VIA-006 | AC-006.2 | DecisionApiIT.cincoDias | T009, T010 | Pasa (13/11) |
| REQ-VIA-007 | AC-007.1 | DecisionApiIT.confirmaYAvisa | T009 a T012 | Pasa (13/11) |
| REQ-VIA-007 | AC-007.2 | DecisionApiIT.sinDisponibilidad | T009, T010 | Pasa (13/11) |
| REQ-VIA-008 | AC-008.1 | DecisionApiIT.rechazoSinMotivo | T009, T010 | Pasa (13/11) |
| REQ-VIA-008 | AC-008.2 | DecisionApiIT.rechazoConMotivo | T009 a T011 | Pasa (13/11) |
| REQ-VIA-009 | AC-009.1 | AssignmentApiIT.hotelSinDisponibilidad | T013, T014 | Pasa (13/11) |
| REQ-VIA-009 | AC-009.2 | AssignmentApiIT.cruceGuia | T013, T014 | Pasa (13/11) |
| REQ-VIA-009 | AC-009.3 | AssignmentApiIT.cruceConductor | T013, T014 | Pasa (13/11) |
| REQ-VIA-012 | AC-012.2 | DecisionApiIT.otrosRolesNoConfirman, AssignmentApiIT.soloCoordinador, hotelAjeno | T011, T015 | Pasa (13/11) |
| NFR-VIA-003 | Aviso < 60 s | DecisionApiIT.avisoEnMenosDe60s | T012 | Pasa (13/11) |
| Contrato | DoD 9 | ContractTest.contratoIgualALaApi | T008, T011, T015 | Pasa |
| REQ-VIA-010 | AC-010.1, 010.2 | Sprint 3 | T016 | Pendiente |

Cobertura: 11 de 12 requisitos funcionales con criterio y prueba en verde (001 a 009, 011 y 012), más NFR-VIA-003. Queda REQ-VIA-010 (cancelar) para el Sprint 3. 45 pruebas automáticas en verde.
