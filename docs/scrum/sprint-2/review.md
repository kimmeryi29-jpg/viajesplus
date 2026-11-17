# Sprint Review · Sprint 2

Viernes 13 de noviembre de 2026, 1 hora · Asisten: Kevin Santiago Rincón Jaimes (PO), Meryi Yimet Bueno Mantilla (SM y Developers) y usuarios clave (agente de reservas, coordinador de tours, proveedor y gerente)

**Sprint Goal:** un agente de reservas puede confirmar o rechazar una reserva y el coordinador puede asignarle hotel y tour, aplicando las reglas de cupo, antelación, disponibilidad del proveedor y cruces de guía y conductor. **Cumplido.**

## Qué se demostró (guion de D11)
| PBI | Demostración | Resultado |
|---|---|---|
| PBI-04 · HU2 | Solicitud de 3 viajeros que aparta el cupo; confirmación del agente y aviso al cliente; una salida a 4 días: ANTELACION_INSUFICIENTE; Santa Marta del 26 de febrero sin hotel confirmado: SIN_DISPONIBILIDAD; rechazo sin motivo: MOTIVO_OBLIGATORIO; rechazo con motivo que libera el cupo | Aceptado |
| PBI-05 · HU3 | El proveedor confirma disponibilidad y tarifa; el coordinador asigna hotel, guía y conductor; el mismo guía en otra salida ese día: CRUCE_GUIA; avisos al guía y al conductor | Aceptado |

Entorno de pruebas: https://viajesplus.onrender.com (puerto 8080) · API: /swagger-ui.html · 45 pruebas automáticas en verde.

## Retroalimentación de los usuarios clave
- El agente confirmó que ver las plazas libres y los días de antelación en la bandeja le ahorra llamadas (pedido de la Review del Sprint 1, ya incluido).
- El gerente pidió que al cancelar se devuelva el dinero según la antelación: 100 % con 15 días o más, 50 % entre 7 y 14 días y 0 % con menos de 7.

## Cambios al Product Backlog
- **Nace PBI-09 · CR-001 «Cancelar con reembolso»** (Should). Se refina y estima el lunes 16 (D13) y entra al Sprint 3 por un delta de la spec (D14).
- PBI-06 y PBI-07 quedan listos (D10). PBI-08 sigue como objetivo extendido.

**Velocidad del sprint: 13 puntos (acumulado 21).** Burndown: 13, 13, 13, 5, 0.
