#!/usr/bin/env bash
set -euo pipefail

TARGET="${TARGET:-http://127.0.0.1:${APP_HTTP_PORT:-18090}/actuator/health}"
INTERVAL="${INTERVAL:-5}"

printf 'Polling %s every %ss. Press Ctrl+C to stop.\n' "$TARGET" "$INTERVAL"

while true; do
  timestamp="$(date -u +"%Y-%m-%dT%H:%M:%SZ")"
  tempfile="$(mktemp)"
  if http_code="$(curl -sS -o "$tempfile" -w "%{http_code}" "$TARGET")"; then
    status="$(grep -o "\"status\"[[:space:]]*:[[:space:]]*\"[^\"]*\"" "$tempfile" | head -n1 | cut -d\" -f4 || true)"
    status="${status:-UNKNOWN}"
    printf '%s %s status=%s code=%s\n' "$timestamp" "$TARGET" "$status" "$http_code"
  else
    printf '%s %s is unreachable (curl exit %s)\n' "$timestamp" "$TARGET" "$?"
  fi
  rm -f "$tempfile"
  sleep "$INTERVAL"
done
