# ADR-002 · El cupo se valida en el servicio y se protege en la base de datos

Estado: Aceptada · Puerta 2, 29 de octubre de 2026 · Tarjeta D06 · Requisitos: REQ-VIA-005, RN-03

## Contexto
Dos agentes pueden confirmar al mismo tiempo reservas de la misma salida. Validar solo en el servicio deja una condición de carrera.

## Decisión
Las plazas se ocupan con una sola sentencia condicionada (`UPDATE ... WHERE occupied_seats + n <= capacity`) y la tabla `departure` tiene la restricción `ck_departure_cupo` (0 ≤ occupied_seats ≤ capacity).

## Alternativa descartada
Solo en el servicio: dos transacciones leen el mismo cupo y ambas confirman.

## Consecuencias
Si la sentencia no actualiza filas, el servicio responde CUPO_INSUFICIENTE (Sprint 2). Probado en T003 con dos operaciones simultáneas (AC-005.1).
