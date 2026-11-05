# Sprint Review · Sprint 1

Viernes 6 de noviembre de 2026, 1 hora · Asisten: Kevin Santiago Rincón Jaimes (PO), Meryi Yimet Bueno Mantilla (SM y Developers) y usuarios clave de la agencia (agente de reservas, asesor y gerente)

**Sprint Goal:** al terminar el sprint, existe un esqueleto técnico que compila y se despliega, y un cliente puede solicitar una reserva (queda PENDIENTE) en el entorno de pruebas. **Cumplido.**

## Qué se terminó (cumple la DoD)
| PBI | Qué se demostró | Demostrado |
|---|---|---|
| PBI-02 · Esqueleto técnico | CI en verde con 21 pruebas, migraciones Flyway, restricción de cupo con dos operaciones simultáneas, inicio de sesión con los 10 roles, 403 para roles sin permiso y auditoría | Sí |
| PBI-03 · HU1 Solicitar una reserva | Cotización de Cartagena para 3 viajeros = $3.600.000 (cliente y asesor), solicitud que queda PENDIENTE, error 422 sin fecha o con 0 viajeros, «Mis reservas» y la auditoría vista por el gerente | Sí |

Entorno de pruebas: https://viajesplus.onrender.com · API: /swagger-ui.html

## Qué NO se terminó y por qué
- El inicio de sesión usa el proveedor OIDC simulado; el tenant real llega con TI en el Sprint 3 (IMP-01, D21).

## Retroalimentación de los usuarios clave
- El agente pidió ver las plazas libres de cada salida al decidir (ya se ven en el detalle del paquete; se tendrá en cuenta en la bandeja del Sprint 2).
- El gerente pidió que la auditoría muestre la hora de Colombia (ya la muestra).

## Cambios al Product Backlog
- Sin cambios de orden. PBI-04 y PBI-05 entran al Sprint 2 (13 puntos).

**Velocidad del sprint: 8 puntos** · Burndown en 0 al cierre del viernes.
