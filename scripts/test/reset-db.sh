#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
cd "$ROOT_DIR"

DB_HOST="${G4IMS_TEST_DB_HOST:-127.0.0.1}"
DB_PORT="${G4IMS_TEST_DB_PORT:-3307}"
DB_NAME="${G4IMS_TEST_DB_NAME:-g4ims_test}"
DB_USER="${G4IMS_TEST_DB_USER:-g4ims_test}"
DB_PASS="${G4IMS_TEST_DB_PASSWORD:-g4ims_test}"

MYSQL=(mysql -h"$DB_HOST" -P"$DB_PORT" -u"$DB_USER" -p"$DB_PASS" "$DB_NAME")

echo "Resetting schema on ${DB_NAME}@${DB_HOST}:${DB_PORT}..."
"${MYSQL[@]}" -e "SET FOREIGN_KEY_CHECKS=0;"
TABLES=$("${MYSQL[@]}" -N -e "SELECT table_name FROM information_schema.tables WHERE table_schema='${DB_NAME}';" || true)
if [ -n "$TABLES" ]; then
  while IFS= read -r table; do
    [ -z "$table" ] && continue
    "${MYSQL[@]}" -e "DROP TABLE IF EXISTS \`${table}\`;"
  done <<< "$TABLES"
fi
"${MYSQL[@]}" -e "SET FOREIGN_KEY_CHECKS=1;"

ENV_FILE="${G4IMS_ENV_FILE:-inventory-server/.env.test}"
if [ ! -f "$ENV_FILE" ]; then
  cp inventory-server/.env.test.example "$ENV_FILE"
fi

echo "Running migrations..."
(
  cd inventory-server
  mvn -q package -DskipTests
  java -Dg4ims.env.file="$(basename "$ENV_FILE")" -jar target/inventory-server-1.0.0.jar migrate
) || {
  echo "Migrate failed; check $ENV_FILE and MySQL on port 3307."
  exit 1
}

FIXTURE="${1:-initialized}"
case "$FIXTURE" in
  initialized)
    "${MYSQL[@]}" < scripts/test/sql/initialized.sql
    ;;
  infra_ready)
    "${MYSQL[@]}" < scripts/test/sql/infra_ready.sql
    ;;
  none)
    ;;
  *)
    echo "Unknown fixture: $FIXTURE (use initialized|infra_ready|none)"
    exit 1
    ;;
esac

echo "Database reset complete (fixture=${FIXTURE})."
