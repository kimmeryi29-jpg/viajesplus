# Especificación: ViajesPlus · Versión 1.0 (aprobada)

Ruta: `specs/001-viajesplus/spec.md` · Tarjetas D03, D04 y D05 · Redacta: Meryi Yimet Bueno Mantilla (Dev Spec)
Aprobada en la Puerta 1 el 28 de octubre de 2026 por Kevin Santiago Rincón Jaimes (Product Owner)

## 1. Contexto y objetivo

La agencia gestiona hoy las reservas de paquetes turísticos por llamada y por chat. No queda un registro único: es difícil saber cuántas plazas quedan en una salida, quién aceptó o rechazó una reserva y por qué.

**Product Goal:** que ninguna reserva de paquete turístico se gestione por llamada o chat informal: toda solicitud queda registrada, validada contra las reglas de la agencia, decidida por un agente de reservas, con hotel y tour asignados, y auditada.

## 2. Actores

| Actor | Qué hace en el sistema |
|---|---|
| Cliente | Cotiza, solicita, consulta y cancela sus reservas |
| Asesor de viajes | Cotiza paquetes y acompaña al cliente |
| Agente de reservas | Confirma o rechaza las reservas |
| Proveedor de hoteles | Confirma disponibilidad y tarifas |
| Coordinador de tours | Asigna hotel, guía y conductor a cada salida |
| Guía turístico | Recibe su grupo y reporta novedades (objetivo extendido) |
| Conductor | Recibe la ruta y los horarios (objetivo extendido) |
| Contador | Recibe el registro de los reembolsos |
| Gerente | Revisa la auditoría, participa en la UAT y aprueba el release |
| Administrador | Gestiona usuarios y roles |

## 3. Alcance del MVP y no-objetivos

**Incluido (tres sprints):** cotizar y solicitar una reserva; confirmar o rechazar con las reglas de cupo, antelación y disponibilidad; asignar hotel, guía y conductor sin cruces; cancelar antes de la salida; auditar cada cambio de estado; rendimiento de la cotización y trazabilidad completa.

**No-objetivos:** pagos y facturas, descuentos y excepciones del Gerente, reportes gerenciales y pantallas de administración de usuarios y catálogo. Quedan como backlog futuro (HU-46 a HU-53). El itinerario para guía y conductor (HU5) es un objetivo extendido que no se inicia.

## 4. Glosario

| Término | Definición |
|---|---|
| Paquete | Producto turístico con destino, duración y tarifa por persona |
| Salida | Fecha en que parte un paquete, con un cupo de plazas |
| Cupo | Número máximo de viajeros de una salida |
| Plazas ocupadas | Viajeros de las reservas confirmadas de una salida |
| Reserva | Solicitud de un cliente para una salida, con número de viajeros y total |
| Antelación | Días calendario entre la fecha de la decisión y la fecha de salida |

## 5. Historias de usuario (HU1 a HU5, con INVEST)

| ID | PBI | Historia | INVEST |
|---|---|---|---|
| HU1 | PBI-03 | Como cliente quiero solicitar una reserva indicando paquete, fecha y viajeros para que quede registrada en estado PENDIENTE. | Independiente, pequeña y comprobable |
| HU2 | PBI-04 | Como agente de reservas quiero confirmar o rechazar una reserva para no vender más plazas de las disponibles. | Valiosa; la mayor (se divide en tareas) |
| HU3 | PBI-05 | Como coordinador de tours quiero asignar hotel, guía y conductor para que cada salida quede cubierta. | Depende de HU2 (aceptable) |
| HU4 | PBI-06 | Como cliente quiero cancelar mi reserva antes de la salida para liberar el cupo. | Pequeña y negociable |
| HU5 | PBI-08 | Como guía turístico quiero ver el itinerario y el grupo de mi salida para recibir a mis viajeros. | Objetivo extendido (no se inicia) |

El detalle está en las 53 historias de la hoja Historias de Usuario del Excel (HU-01 a HU-53).

## 6. Reglas de negocio

| ID | Regla | Origen |
|---|---|---|
| RN-01 | El total de una reserva es la tarifa por persona del paquete multiplicada por el número de viajeros, en pesos colombianos con dos decimales | Política comercial |
| RN-02 | El cupo de una salida lo consumen las reservas PENDIENTE y CONFIRMADA | Agente de reservas (clarificación) |
| RN-03 | Las plazas ocupadas de una salida nunca superan su cupo | Operaciones |
| RN-04 | Una reserva solo se confirma si faltan 5 días calendario o más para la salida | Gerencia |
| RN-05 | Solo se confirma o asigna un hotel con disponibilidad confirmada por el proveedor | Proveedores |
| RN-06 | Un guía o un conductor no puede tener dos salidas el mismo día | Coordinación de tours |
| RN-07 | Un cliente puede cancelar mientras la fecha de salida no haya llegado; el cupo se libera | Gerencia |

## 7. Requisitos funcionales (EARS)

| ID | Patrón | Requisito | Sprint |
|---|---|---|---|
| REQ-VIA-001 | Evento | Cuando un cliente, un asesor o un agente cotiza un paquete con un número de viajeros, el sistema deberá mostrar la tarifa por persona y el total. | S1 |
| REQ-VIA-002 | Ubicuo | El sistema deberá calcular el total de una cotización o reserva según RN-01. | S1 |
| REQ-VIA-003 | Evento | Cuando un cliente envía una solicitud con paquete, fecha de salida programada y viajeros, el sistema deberá crearla en estado PENDIENTE con su total. | S1 |
| REQ-VIA-004 | No deseado | Si la reserva supera las plazas libres de la salida, entonces el sistema deberá rechazar la confirmación con CUPO_INSUFICIENTE. | S2 |
| REQ-VIA-005 | No deseado | Si dos operaciones intentan ocupar la misma plaza al tiempo, entonces el sistema deberá permitir solo una, de modo que las plazas ocupadas nunca superen el cupo. | S1 (base) y S2 |
| REQ-VIA-006 | No deseado | Si faltan menos de 5 días para la salida, entonces el sistema deberá rechazar la confirmación con ANTELACION_INSUFICIENTE. | S2 |
| REQ-VIA-007 | Evento | Cuando el agente confirma una reserva PENDIENTE que cumple las reglas y el proveedor tiene disponibilidad, el sistema deberá pasarla a CONFIRMADA y notificar al cliente; sin disponibilidad deberá responder SIN_DISPONIBILIDAD. | S2 |
| REQ-VIA-008 | Evento | Cuando el agente rechaza una reserva PENDIENTE, el sistema deberá exigir un motivo, pasarla a RECHAZADA y notificar al cliente. | S2 |
| REQ-VIA-009 | No deseado | Si la asignación usa un hotel sin disponibilidad confirmada o un guía o conductor con otra salida ese día, entonces el sistema deberá bloquearla. | S2 |
| REQ-VIA-010 | Evento | Cuando el cliente cancela una reserva cuya fecha de salida no ha llegado, el sistema deberá pasarla a CANCELADA y liberar su cupo. | S3 |
| REQ-VIA-011 | Ubicuo | El sistema deberá registrar en la auditoría cada cambio de estado de una reserva, con usuario, fecha-hora UTC y motivo. | S1 |
| REQ-VIA-012 | Ubicuo | El sistema deberá autorizar cada operación según el rol del usuario y mostrar a cada cliente únicamente sus propias reservas. | S1 |
| REQ-VIA-014 | Evento | Cuando un guía o conductor consulta su salida, el sistema deberá mostrar itinerario, grupo y horarios. | Extendido |
| REQ-VIA-015 | Ubicuo | El sistema deberá mostrar a cada guía y conductor solo las salidas que tiene asignadas. | Extendido |

REQ-VIA-013 queda reservado para el cambio CR-001 (reembolso), que entrará por un delta de esta especificación.

## 8. Requisitos no funcionales (medibles)

| ID | Requisito | Cómo se medirá |
|---|---|---|
| NFR-VIA-001 | La cotización responderá en menos de 300 ms (p95) con 500 usuarios concurrentes durante 5 minutos | Prueba de carga (T017) |
| NFR-VIA-002 | La autenticación se hará con el proveedor OIDC de la agencia y la autorización por rol | Pruebas de acceso y análisis estático |
| NFR-VIA-003 | El cliente será notificado en menos de 60 segundos tras la decisión | Prueba de integración con correo simulado |
| NFR-VIA-004 | Los datos personales se tratarán conforme a la Ley 1581 de 2012 | Revisión de datos y de logs |

## 9. Criterios de aceptación (Given/When/Then)

| ID | Dado | Cuando | Entonces |
|---|---|---|---|
| AC-001.1 | El paquete Cartagena histórica cuesta $1.200.000 por persona | El cliente o el asesor cotiza para 3 viajeros | Ve tarifa $1.200.000 y total $3.600.000 |
| AC-002.1 | Tarifa de $1.200.000 por persona | Se calcula para 3 viajeros | El total es $3.600.000 |
| AC-002.2 (borde) | Tarifa de $650.000 por persona | Se calcula para 1 viajero | El total es $650.000 |
| AC-002.3 (error) | Cualquier paquete | Se calcula para 0 viajeros | Se rechaza con DATOS_INVALIDOS en el campo travelers |
| AC-003.1 | Cartagena tiene salida el 15 de enero de 2027 | El cliente solicita esa salida para 3 viajeros | Se crea la reserva en PENDIENTE con total $3.600.000 |
| AC-003.2 (error) | Un cliente autenticado | Envía la solicitud sin fecha de salida o con 0 viajeros | Responde 422 DATOS_INVALIDOS e indica el campo |
| AC-004.1 | Una salida con 2 plazas libres | El agente confirma una reserva de 3 viajeros | Se rechaza con CUPO_INSUFICIENTE |
| AC-004.2 (borde) | Una salida con 3 plazas libres | El agente confirma una reserva de 3 viajeros | Se confirma y quedan 0 plazas |
| AC-005.1 | Una salida con 1 plaza libre | Dos operaciones la ocupan al mismo tiempo | Solo una lo logra |
| AC-005.2 | Una salida con cupo 2 | Se intenta dejar 3 plazas ocupadas directamente en la base | La base de datos lo rechaza |
| AC-006.1 | Hoy es 10 de noviembre y la salida es el 14 | El agente confirma | Se rechaza con ANTELACION_INSUFICIENTE |
| AC-006.2 (borde) | Hoy es 10 de noviembre y la salida es el 15 | El agente confirma | Se confirma |
| AC-007.1 | Una reserva PENDIENTE que cumple las reglas | El agente la confirma | Pasa a CONFIRMADA y el cliente recibe el aviso |
| AC-007.2 | El proveedor no tiene disponibilidad | El agente confirma | Se rechaza con SIN_DISPONIBILIDAD |
| AC-008.1 | Una reserva PENDIENTE | El agente rechaza sin motivo | Error MOTIVO_OBLIGATORIO |
| AC-008.2 | Una reserva PENDIENTE | El agente rechaza con motivo | Pasa a RECHAZADA |
| AC-009.1 | Un hotel sin disponibilidad confirmada | El coordinador lo asigna | Se bloquea |
| AC-009.2 | Un guía con salida el 4 de diciembre | Se le asigna otra salida ese día | Se bloquea |
| AC-009.3 | Un conductor con salida el 4 de diciembre | Se le asigna otra salida ese día | Se bloquea |
| AC-010.1 | Una reserva cuya salida no ha llegado | El cliente la cancela | Pasa a CANCELADA y se libera el cupo |
| AC-010.2 (error) | Una reserva cuya salida ya llegó | El cliente intenta cancelarla | Responde 422 |
| AC-011.1 | Un cliente crea una reserva | Se consulta la auditoría | Aparece la acción con usuario, fecha-hora y estado nuevo |
| AC-012.1 | La reserva pertenece a otro cliente | Un cliente intenta verla | Responde 403 |
| AC-012.2 | Un cliente autenticado | Intenta confirmar una reserva | Responde 403 |
| AC-012.3 (error) | Credenciales inválidas o sin token | Se llama a la API | Responde 401 |

## 10. Decisiones de clarificación

Ver `docs/scrum/preparacion/acta-clarificacion.md` (D04, 27 de octubre de 2026).

## 11. Historial de versiones

| Versión | Fecha | Cambio | Tarjeta |
|---|---|---|---|
| 0.1 | 26/10/2026 | Contexto, actores, alcance, historias y reglas iniciales | D03 |
| 0.2 | 27/10/2026 | Decisiones de la clarificación; sin marcas [NECESITA ACLARACIÓN] | D04 |
| 1.0 | 28/10/2026 | Requisitos EARS, NFR y criterios Given/When/Then; aprobada en la Puerta 1 | D05 |
