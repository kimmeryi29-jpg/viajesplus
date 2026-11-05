# Registro de impedimentos

## IMP-01 · No hay proveedor OIDC de pruebas
- Fecha y hora: jueves 5 de noviembre de 2026, 4:00 p. m. · Registra: Meryi Yimet Bueno Mantilla (SM)
- Qué pasó: al configurar T004 no hay un tenant OIDC de pruebas de la agencia.
- Impacto: T004 bloqueada; sin ella no hay usuarios autenticados en pruebas. Riesgo al Sprint Goal: alto.
- Dueño: Meryi Yimet Bueno Mantilla · Escalado a: TI de ViajesPlus
- Alternativa: proveedor OIDC simulado dentro de la aplicación, solo para pruebas, con los mismos claims (sub, name, roles). Revisado contra la constitución (principio 4) y anotado en plan.md (sección 6) y ADR-003.
- Fecha límite: martes 17 de noviembre de 2026 (D21, Sprint 3) · Estado: mitigado
- Cierre: pendiente; se cierra al configurar VIAJESPLUS_OIDC_ISSUER_URI con el tenant real.

## IMP-02 · El glosario contradice RN-02 sobre cuándo se descuenta el cupo
- Previsto para el Sprint 2 (T010 y T011). Estado: previsto.
