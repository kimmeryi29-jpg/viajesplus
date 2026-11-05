# Constitución de ViajesPlus

Versión 1.0 · Aprobada en la Puerta 0 el 26 de octubre de 2026 (tarjeta D02)
Redacta: Meryi Yimet Bueno Mantilla (Dev Spec) · Ratifica: Kevin Santiago Rincón Jaimes (Product Owner)

Estos principios no se negocian durante el proyecto. Si una tarea o una sugerencia de la IA choca con alguno, gana el principio y se discute en un ADR.

| # | Principio | Qué evita |
|---|---|---|
| 1 | Ninguna funcionalidad sin criterios de aceptación aprobados en spec.md | Código sin forma de verificarse |
| 2 | Las pruebas se escriben antes del código y no se cambian para que pasen | Pruebas que se adaptan al código en lugar de a la spec |
| 3 | Simplicidad: monolito modular; ninguna dependencia nueva sin un ADR | Arquitecturas y librerías que nadie pidió |
| 4 | Seguridad por defecto y mínimo privilegio: cada ruta declara qué rol la usa | Endpoints abiertos y secretos en el repositorio |
| 5 | Todo cambio de estado de una reserva se audita con usuario, fecha-hora UTC y motivo | Pérdida de trazabilidad de negocio |
| 6 | Los datos personales se tratan según la Ley 1581 de 2012: nunca en logs ni en pruebas | Incumplimiento legal y fugas de datos |
| 7 | Todo cambio que llega a main referencia una tarea (T###) o una tarjeta (D##) | Cambios que no se pueden rastrear a un requisito |

## Cómo se usa

- Cada puerta SDD revisa el documento contra esta lista.
- El revisor del diff marca en el pull request qué principio verificó.
- Cambiar un principio exige un ADR aprobado por la PO.
