#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
SOURCE="${FRONTEND_HTML_SOURCE:-/Users/leixingbang/sanxiaProject/nginx/nginx/html}"
DEST="${FRONTEND_HTML_DEST:-$ROOT_DIR/deploy/docker/nginx/html}"

if [[ ! -d "$SOURCE" ]]; then
  echo "Frontend html source does not exist: $SOURCE" >&2
  exit 1
fi

mkdir -p "$DEST"
rsync -a --delete --exclude='.DS_Store' "$SOURCE"/ "$DEST"/
du -sh "$DEST"
