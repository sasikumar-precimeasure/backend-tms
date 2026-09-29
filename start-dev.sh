#!/usr/bin/env bash
# Starts a local Postgres (if not already running) and this backend, both in
# parallel with whatever else you're running (frontend dev server, Modbus
# gateway, etc.) - for local development only, not the client-premises
# deployment path (see README.md for that: java -jar / Docker).
set -euo pipefail

cd "$(dirname "$0")"

CONTAINER_NAME="tms-backend-postgres"
DB_PORT="${DB_PORT:-5434}"

if ! docker ps --filter "name=${CONTAINER_NAME}" --filter status=running -q | grep -q .; then
  if docker ps -a --filter "name=${CONTAINER_NAME}" -q | grep -q .; then
    echo "Starting existing ${CONTAINER_NAME} container..."
    docker start "${CONTAINER_NAME}" >/dev/null
  else
    echo "Creating ${CONTAINER_NAME} container on port ${DB_PORT}..."
    docker run -d --name "${CONTAINER_NAME}" \
      -e POSTGRES_USER=tms -e POSTGRES_PASSWORD=tms -e POSTGRES_DB=tms \
      -p "${DB_PORT}:5432" postgres:16 >/dev/null
  fi
fi

echo "Waiting for Postgres to accept connections..."
until docker exec "${CONTAINER_NAME}" pg_isready -U tms >/dev/null 2>&1; do
  sleep 1
done
echo "Postgres ready on port ${DB_PORT}."

export DB_URL="jdbc:postgresql://localhost:${DB_PORT}/tms"
export DB_USERNAME="tms"
export DB_PASSWORD="tms"

echo "Starting tms-backend (Spring Boot) on \$SERVER_PORT=${SERVER_PORT:-8080}..."
exec ./gradlew bootRun --console=plain
