# ADR-004 · Notificaciones mediante eventos internos con reintento

Estado: Aceptada · Puerta 2, 29 de octubre de 2026 · Requisito: NFR-VIA-003

## Contexto
El cliente debe recibir la decisión sobre su reserva en menos de 60 segundos (Sprint 2).

## Decisión
La decisión publica un evento interno de Spring; un componente lo escucha después del commit y envía el correo con hasta tres reintentos.

## Alternativa descartada
Un broker de mensajes: complejidad innecesaria para el volumen actual.

## Consecuencias
Si el correo falla tres veces queda registrado para reenviarlo; la decisión no se revierte.
