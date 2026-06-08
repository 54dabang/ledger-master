#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
OUTPUT="${OUTPUT:-$ROOT_DIR/deploy/docker/mysql/initdb/001-ledger-manager.sql.gz}"
MYSQL_CONTAINER="${MYSQL_CONTAINER:-mysql-2}"
MYSQL_USER="${MYSQL_USER:-root}"
MYSQL_PASSWORD="${MYSQL_PASSWORD:-oneapimmysql}"
MYSQL_DATABASES="${MYSQL_DATABASES:-ledger manager}"

read -r -a DB_ARRAY <<< "$MYSQL_DATABASES"
mkdir -p "$(dirname "$OUTPUT")"

docker exec -e MYSQL_PWD="$MYSQL_PASSWORD" "$MYSQL_CONTAINER" \
  mysqldump -u"$MYSQL_USER" \
    --databases "${DB_ARRAY[@]}" \
    --single-transaction \
    --quick \
    --routines \
    --triggers \
    --events \
    --hex-blob \
    --default-character-set=utf8mb4 \
    --set-gtid-purged=OFF \
  | gzip -9 > "$OUTPUT"

gzip -t "$OUTPUT"
ls -lh "$OUTPUT"
