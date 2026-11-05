# Working agreement, DoR y DoD · Versión 1

Tarjeta D01 · Lunes 2 de noviembre de 2026 (Sprint Planning del Sprint 1) · Responsable: Meryi Yimet Bueno Mantilla (SM) · Revisa: equipo

## Acuerdos del equipo
- Trabajo de lunes a viernes; Daily de 15 minutos a las 7:00 p. m. por videollamada, antes de cerrar el día.
- Las dudas de negocio se le preguntan a la PO por el grupo del equipo, no por mensajes privados; los acuerdos se escriben en la spec.
- Nada llega a main sin una tarea asociada (T### o D##) y la revisión de la otra persona. Pruebas antes que código.
- La IA se usa con el prompt de seis partes, nunca con datos reales; el diff se revisa completo y se anota en la bitácora.
- Los impedimentos se avisan a la SM apenas aparecen, sin esperar al Daily.
- El tablero se actualiza antes del Daily; En curso máximo 4 tarjetas y En revisión máximo 3; nadie mueve su propia tarjeta a Hecho.
- Se critica el trabajo, no a la persona; las discrepancias técnicas se resuelven con un ADR.

## Definition of Ready (6 condiciones)
| # | Condición para que un ítem esté listo | Quién la verifica |
|---|---|---|
| 1 | Tiene una intención clara y su valor («para …») | PO |
| 2 | Tiene al menos dos criterios de aceptación gruesos, uno de borde o de error | PO y Dev QA |
| 3 | Es lo bastante pequeño para terminarse en un sprint y está estimado | Developers |
| 4 | Las dependencias están identificadas y ninguna bloquea desde fuera del equipo | SM |
| 5 | Se sabe a qué usuario clave preguntar por las reglas de negocio | PO |
| 6 | No exige la spec detallada: esa se redacta y aprueba con su puerta | Todos |

## Definition of Done (12 condiciones)
| # | Condición | Cómo se verifica |
|---|---|---|
| 1 | La spec del ítem fue aprobada en su puerta y no tiene marcas [NECESITA ACLARACIÓN] | Acta de la puerta |
| 2 | Cada criterio de aceptación tiene al menos una prueba automatizada, incluidos los bordes | Matriz de trazabilidad |
| 3 | Las pruebas se escribieron antes del código y están en verde en CI | Historial y GitHub Actions |
| 4 | El código llegó a main con una tarea (T###) y lo revisó la otra persona | Commit y revisión |
| 5 | El diff no incluye funcionalidad ni dependencias que ningún requisito pidió | Revisión del diff |
| 6 | Todo cambio de estado de una reserva queda en la auditoría (REQ-VIA-011) | Prueba de auditoría |
| 7 | Sin hallazgos críticos o altos en el análisis estático y sin secretos en el repositorio | Informe de SAST |
| 8 | Datos personales según la Ley 1581: logs sin datos de clientes | Lista de verificación de Dev Infra |
| 9 | El contrato OpenAPI está actualizado y la prueba de contrato pasa | ContractTest en CI |
| 10 | La matriz de trazabilidad incluye el ítem | docs/trazabilidad.md |
| 11 | El uso de IA quedó anotado en la bitácora | docs/ia/bitacora.md |
| 12 | Está desplegado en el entorno de pruebas y se puede demostrar en la Review | URL de Render |
