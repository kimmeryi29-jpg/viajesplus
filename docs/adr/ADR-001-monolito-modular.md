# ADR-001 · Monolito modular con capas hexagonales

Estado: Aceptada · Puerta 2, 29 de octubre de 2026 · Tarjeta D06

## Contexto
El equipo tiene dos personas y tres sprints de una semana. Hay un solo producto y un solo entorno de pruebas.

## Decisión
Una sola aplicación Spring Boot con paquetes `api`, `application`, `domain`, `infrastructure` y `web`. El dominio no depende de Spring.

## Alternativa descartada
Microservicios: costo operativo alto (varios despliegues, bases y redes) para un equipo pequeño y sin necesidad de escalar partes por separado.

## Consecuencias
Un solo despliegue y una sola base. Si en el futuro hace falta separar un módulo, las capas ya marcan el límite.
