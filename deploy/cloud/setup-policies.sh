#!/bin/bash
# Políticas de retención del clúster (EP4-11). Ejecutar una vez que el clúster esté arriba.
set -e

# Colas principales: máximo 1000 mensajes; si se llena se descarta el más antiguo
docker exec rabbit1 rabbitmqctl set_policy pedidos-colas '^pedidos\..*\.queue$' \
  '{"max-length":1000,"overflow":"drop-head"}' --apply-to queues

# DLQ: los mensajes fallidos se conservan 24 horas (TTL) y como máximo 500
docker exec rabbit1 rabbitmqctl set_policy pedidos-dlq '^pedidos\..*\.dlq$' \
  '{"message-ttl":86400000,"max-length":500}' --apply-to queues

docker exec rabbit1 rabbitmqctl list_policies
