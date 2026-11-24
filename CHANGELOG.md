# Changelog

Formato basado en [Keep a Changelog](https://keepachangelog.com/es-ES/1.1.0/) y versionado [SemVer](https://semver.org/lang/es/).

## [Unreleased]

### Agregado
- Consumidores RabbitMQ independientes para notificaciones, tickets y auditoría, con ACK manual.
- Consumidor de las 3 DLQ que registra los mensajes no entregados.
- Consumidores Kafka en 3 consumer groups (`notificaciones-group`, `analitica-group`, `estado-group`) con manejo de errores y reintentos.
- Endpoints de consulta `/api/notifications/kafka/analitica` y `/estados`.
- Dockerfile.

### Corregido
- Los consumidores de notificaciones y tickets compartían la misma cola, por lo que cada pedido lo procesaba solo uno de ellos.
