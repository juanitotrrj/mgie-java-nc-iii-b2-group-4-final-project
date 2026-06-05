#!/usr/bin/env bash
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT"

PROFILE="${1:-full}"
SKIP_DOCKER="${SKIP_DOCKER:-0}"

if [ "$SKIP_DOCKER" != "1" ]; then
  docker compose -f docker-compose.test.yml up -d
  echo "Waiting for MySQL..."
  sleep 8
fi

if [ ! -f inventory-server/.env.test ]; then
  cp inventory-server/.env.test.example inventory-server/.env.test
fi

export G4IMS_ENV_FILE=inventory-server/.env.test

echo "=== Unit tests (server + client) ==="
mvn -q test -pl inventory-server,inventory-client

case "$PROFILE" in
  unit-only)
    exit 0
    ;;
  integration)
    mvn -q verify -Pintegration -pl inventory-server -Dg4ims.env.file=.env.test
    exit 0
    ;;
  api)
    mvn -q verify -Papi -pl inventory-server -Dg4ims.env.file=.env.test
    exit 0
    ;;
  e2e)
    mvn -q test -Pe2e -pl test-automation -Dg4ims.env.file=../inventory-server/.env.test
    exit 0
    ;;
  uat)
    mvn -q test -Puat -pl test-automation -Dtest=CucumberUatRunner -Dg4ims.env.file=../inventory-server/.env.test
    exit 0
    ;;
  perf)
    mvn -q test -Pperf -pl inventory-server
    exit 0
    ;;
  load)
    mvn -q verify -Pload -pl test-automation
    exit 0
    ;;
  full|*)
    mvn -q verify -Pintegration -pl inventory-server -Dg4ims.env.file=.env.test || true
    mvn -q verify -Papi -pl inventory-server -Dg4ims.env.file=.env.test || true
    mvn -q test -Pe2e -pl test-automation -Dg4ims.env.file=../inventory-server/.env.test || true
    mvn -q test -Pperf -pl inventory-server || true
    ;;
esac

echo "Done (profile=$PROFILE)."
