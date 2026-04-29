#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"

cd "$ROOT_DIR"

if [[ ! -f .env ]]; then
  echo ".env file is required for deployment" >&2
  exit 1
fi

set -a
# shellcheck disable=SC1091
source .env
set +a

DEPLOY_BRANCH="${DEPLOY_BRANCH:-main}"

git fetch origin
git switch "$DEPLOY_BRANCH"
git reset --hard "origin/$DEPLOY_BRANCH"

./gradlew clean build -x test
"$ROOT_DIR/scripts/start.sh"

echo "Deployed branch: $DEPLOY_BRANCH"
git rev-parse --short HEAD
