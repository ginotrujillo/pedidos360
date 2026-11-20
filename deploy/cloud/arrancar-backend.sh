#!/bin/bash
# Arranca el backend en la EC2 con el perfil "cloud", conectado a los 2 nodos del clúster.
# Lee usuario y clave de RabbitMQ desde el mismo .env del clúster (no se escriben en el repo).
set -e
cd "$(dirname "$0")"
set -a; source .env; set +a

JAR=$(ls ../../target/pedidos360-*.jar | grep -v original | head -1)

pkill -f pedidos360 || true   # evita dos instancias peleando por el puerto 8080
sleep 2

RABBITMQ_ADDRESSES="localhost:5672,localhost:5673" \
RABBITMQ_USER="$RABBITMQ_USER" \
RABBITMQ_PASSWORD="$RABBITMQ_PASS" \
nohup java -jar "$JAR" --spring.profiles.active=cloud "$@" > ~/app.log 2>&1 &

echo "Backend iniciado. Ver log con:  tail -f ~/app.log"
