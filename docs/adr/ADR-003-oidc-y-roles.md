# ADR-003 · Autenticación OIDC con tokens JWT y autorización por rol

Estado: Aceptada · Puerta 2, 29 de octubre de 2026 · Ajustada por IMP-01 el 5 de noviembre · Requisitos: REQ-VIA-012, NFR-VIA-002

## Contexto
La agencia usará su proveedor de identidad (OIDC). El sistema tiene 10 roles.

## Decisión
La API es un servidor de recursos que valida tokens JWT y lee el rol del claim `roles`. Cada ruta declara su rol en `SecurityConfig` (mínimo privilegio). Las pantallas usan sesión con los mismos usuarios.

## Alternativa descartada
Usuarios y contraseñas propios del sistema: duplica la gestión de identidades de la agencia.

## Consecuencias
Mientras no exista el tenant de pruebas (IMP-01) se usa un proveedor simulado que emite tokens con los mismos claims. Se reemplaza en el Sprint 3 (D21) configurando `VIAJESPLUS_OIDC_ISSUER_URI`.
