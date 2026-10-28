#!/usr/bin/env bash
# Installs or updates Risktionary on a server using the published GitHub images.
#
# Usage:
#   ./install.sh                  # deploy the latest release
#   IMAGE_TAG=1.2.3 ./install.sh  # deploy a specific version (no leading "v")
#
# Optional overrides: INSTALL_DIR (default /opt/risktionary), BRANCH (default main),
# COMPOSE_URL (where to download docker-compose.yml from; file:// URLs work)
set -euo pipefail

REPO="coreyh-nz/risktionary"
BRANCH="${BRANCH:-main}"
INSTALL_DIR="${INSTALL_DIR:-/opt/risktionary}"
export IMAGE_TAG="${IMAGE_TAG:-latest}"

COMPOSE_URL="${COMPOSE_URL:-https://raw.githubusercontent.com/${REPO}/${BRANCH}/docker-compose.yml}"

command -v docker >/dev/null || { echo "Docker is not installed." >&2; exit 1; }
docker compose version >/dev/null 2>&1 || { echo "Docker Compose v2 is not installed." >&2; exit 1; }

mkdir -p "$INSTALL_DIR"
cd "$INSTALL_DIR"

echo "Downloading docker-compose.yml from ${REPO}@${BRANCH}..."
curl -fsSL "$COMPOSE_URL" -o docker-compose.yml.new
mv docker-compose.yml.new docker-compose.yml

if [[ ! -f backend.env ]]; then
  cat >&2 <<MSG
Missing ${INSTALL_DIR}/backend.env

Create it with the backend environment variables - see the
Backend section of the README - then run this script again.
MSG
  exit 1
fi

echo "Pulling images (tag: ${IMAGE_TAG})..."
if ! docker compose pull; then
  echo "Pull failed" >&2
  exit 1
fi

echo "Stopping existing containers..."
docker compose down --remove-orphans

echo "Starting containers..."
docker compose up -d

docker image prune -f >/dev/null
docker compose ps
echo "Done. Backend: http://localhost:8080  Frontend: http://localhost:3000"
