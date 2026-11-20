#!/bin/bash
# Solo si "cluster_status" NO muestra los dos nodos: une rabbit2 al clúster de rabbit1 a mano.
set -e
docker exec rabbit2 rabbitmqctl stop_app
docker exec rabbit2 rabbitmqctl reset
docker exec rabbit2 rabbitmqctl join_cluster rabbit@rabbit1
docker exec rabbit2 rabbitmqctl start_app
docker exec rabbit1 rabbitmqctl cluster_status
