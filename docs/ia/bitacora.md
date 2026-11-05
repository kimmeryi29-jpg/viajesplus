# Bitácora de uso de IA

Cada entrada registra la tarea, quién dirigió a la IA, el prompt de seis partes, qué se aceptó y qué revisó la otra persona.

## T001 · Proyecto Spring Boot y CI · 4 de noviembre de 2026
- Dirige: Kevin Santiago Rincón Jaimes · Revisa el diff: Meryi Yimet Bueno Mantilla
- Contexto: AGENTS.md, constitution.md y plan.md sección 2. Tarea: pom.xml con Java 21 y Spring Boot 3, paquetes de plan.md y flujo de GitHub Actions.
- Restricciones: solo las dependencias del plan. Criterio de salida: `mvn verify` en verde en CI.
- Revisión: se verificó que el pom.xml solo trae las dependencias del plan (Constitución 3) y que la CI ejecuta todas las pruebas.

## T003 · Migración V1 y restricción de cupo · 5 y 6 de noviembre
- Dirige: Kevin · Revisa: Meryi
- Referencias: REQ-VIA-005, REQ-VIA-011, RN-03, ADR-002. Criterio de salida: AC-005.1 y AC-005.2 en verde.
- Revisión: se verificó que el cupo se ocupa con una sola sentencia condicionada, como dice ADR-002, y no con una lectura seguida de una escritura (condición de carrera).

## T004 · Seguridad OIDC y 10 roles · 5 y 6 de noviembre
- Dirige: Kevin · Revisa: Meryi
- Referencias: REQ-VIA-012, NFR-VIA-002, ADR-003, IMP-01. Criterio de salida: AC-012.1 a AC-012.3.
- Revisión: se confirmó que el proveedor simulado no guarda contraseñas en claro (BCrypt) y que cada ruta declara su rol.

## T007 · Implementar QuoteCalculator · 6 de noviembre
- Dirige: Kevin · Revisa: Meryi
- Prompt de seis partes:
  - Contexto: ViajesPlus, fase 2 de tasks.md; lee AGENTS.md y constitution.md.
  - Tarea: T007, clase `co.viajesplus.domain.QuoteCalculator` con el método `calculate(BigDecimal, int)`.
  - Referencias: spec.md RN-01, REQ-VIA-002, AC-002.1 a AC-002.3.
  - Restricciones: Java 21 sin dependencias nuevas; dinero en BigDecimal con dos decimales.
  - Criterio de salida: las 4 pruebas de `QuoteCalculatorTest` (T006) en verde.
  - Límites: no modifiques las pruebas ni otras clases; resume los archivos tocados.
- Revisión: se verificó que no hay lógica de descuentos (fuera del alcance) y que el redondeo es a dos decimales.

## T008 · ReservationService y endpoints · 6 de noviembre
- Dirige: Kevin · Revisa: Meryi
- Referencias: REQ-VIA-001, 003, 011 y 012; AC-001.1, AC-003.1 y AC-003.2; openapi.yaml.
- Revisión: se verificó que la solicitud no ocupa cupo en el Sprint 1 (eso es de la confirmación, Sprint 2) y que la auditoría va en la misma transacción.
