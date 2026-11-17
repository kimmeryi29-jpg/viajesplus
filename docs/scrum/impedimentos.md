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
- Fecha y hora: miércoles 11 de noviembre de 2026, 2:30 p. m. · Registra: Meryi Yimet Bueno Mantilla (SM)
- Qué pasó: al implementar T010, el glosario decía «plazas ocupadas = reservas confirmadas» y RN-02 decía que las PENDIENTE y CONFIRMADA consumen el cupo.
- Impacto: T010 y T011 bloqueadas menos de una hora. Riesgo al Sprint Goal: medio.
- Dueño: Meryi Yimet Bueno Mantilla · Escalado a: Kevin Santiago Rincón Jaimes (PO)
- Decisión (20 minutos): manda RN-02. Las reservas PENDIENTE y CONFIRMADA ocupan cupo; el cupo se aparta al solicitar y se libera al rechazar o cancelar.
- Cierre: spec v1.0.1 con el glosario corregido, REQ-VIA-004 ajustado y AC-004.3 (D09); migración V3 recalcula las plazas. Estado: **cerrado** el 11/11/2026.
