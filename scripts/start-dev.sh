#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "$0")/.." && pwd)"
cd "$ROOT_DIR"

echo "Building LightShop plugin..."
mvn -B -DskipTests clean package

echo "Preparing plugins folder..."
mkdir -p docker/plugins
rm -f docker/plugins/LightShop-*.jar
cp target/LightShop-*.jar docker/plugins/
rm -f docker/plugins/*-obfuscated.jar

echo "Starting Paper server..."
docker compose -f docker/dev-docker-compose.yml up -d

echo "Waiting for server logs..."
docker logs -f lightshop-mc
