#!/usr/bin/env sh
set -eu

BACKUP_DIR="${BACKUP_DIR:-./backups}"
TIMESTAMP="$(date +%Y%m%d_%H%M%S)"
BACKUP_FILE="${BACKUP_DIR}/ai_design_platform_${TIMESTAMP}.sql.gz"

mkdir -p "${BACKUP_DIR}"
docker compose exec -T mysql sh -c \
  'exec mysqldump --single-transaction --routines --triggers -uroot -p"$MYSQL_ROOT_PASSWORD" ai_design_platform' \
  | gzip > "${BACKUP_FILE}"

find "${BACKUP_DIR}" -type f -name 'ai_design_platform_*.sql.gz' -mtime +14 -delete
printf 'Backup created: %s\n' "${BACKUP_FILE}"
