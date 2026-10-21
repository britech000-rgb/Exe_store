#!/bin/sh
set -eu

if [ -z "${VERCEL_TOKEN:-}" ]; then
  echo "VERCEL_TOKEN is not set. The token is not decided yet."
  echo "When it is ready, run: VERCEL_TOKEN=... npm run deploy"
  exit 1
fi

npx --yes vercel@latest deploy --prod --yes --token "$VERCEL_TOKEN"
