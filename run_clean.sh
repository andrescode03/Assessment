#!/bin/bash
echo "🧹 Limpiando procesos en puerto 8082..."
fuser -k 8082/tcp > /dev/null 2>&1 || true
sleep 2

echo "🚀 Iniciando CoopCredit Application..."
./mvnw spring-boot:run
