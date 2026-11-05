# AGENTS.md · Instrucciones para agentes de IA (ViajesPlus)

Versión 1.0 · Puerta 0, 26 de octubre de 2026 (D02) · Redacta: Kevin Santiago Rincón Jaimes (Dev Infra)

1. Lee primero `constitution.md` y la sección de `specs/001-viajesplus/spec.md` que cita la tarea.
2. Trabaja solo en la tarea indicada (T###). No agregues funcionalidades ni dependencias nuevas; si hacen falta, propón un ADR en `docs/adr/`.
3. Las pruebas existen antes del código: no las modifiques para que pasen.
4. No uses datos personales reales ni escribas secretos en el repositorio. Los usuarios de prueba son ficticios.
5. Respeta la estructura de paquetes de `plan.md`: `api`, `application`, `domain`, `infrastructure` y `web`.
6. Al terminar, resume qué archivos tocaste y qué criterio (AC-###.#) cubre cada cambio. Ese resumen va en la bitácora (`docs/ia/bitacora.md`).

## Comandos

- Compilar y probar: `mvn verify` (las pruebas de integración levantan PostgreSQL 16 con Testcontainers).
- Base local para ejecutar: `docker compose up -d` y luego `mvn spring-boot:run`.
