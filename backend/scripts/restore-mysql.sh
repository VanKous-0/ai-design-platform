#!/usr/bin/env sh
set -eu

if [ "$#" -ne 1 ]; then
  echo "Usage: $0 <backup.sql.gz>" >&2
  exit 1
fi

BACKUP_FILE="$1"
test -f "${BACKUP_FILE}"

gzip -dc "${BACKUP_FILE}" | docker compose exec -T mysql sh -c \
  'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD" ai_design_platform'

echo "Restore completed from ${BACKUP_FILE}"
