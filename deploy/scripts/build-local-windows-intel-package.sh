#!/usr/bin/env bash
set -euo pipefail

ROOT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
IMAGE_TAG="${IMAGE_TAG:-3.9.0-linux-amd64}"
DOCKER_MIRROR="${DOCKER_MIRROR:-docker.1ms.run}"
REDIS_SOURCE_IMAGE="${REDIS_SOURCE_IMAGE:-docker.m.daocloud.io/library/redis:7.2-alpine}"
REFRESH_DB="${REFRESH_DB:-0}"
REFRESH_FRONTEND="${REFRESH_FRONTEND:-0}"
SKIP_MAVEN="${SKIP_MAVEN:-0}"
SKIP_ZIP_TEST="${SKIP_ZIP_TEST:-0}"
DIST_DIR="$ROOT_DIR/dist/ledger-windows-intel"
ZIP_NAME="ledger-windows-intel-docker-${IMAGE_TAG}.zip"

cd "$ROOT_DIR"

need() {
  if ! command -v "$1" >/dev/null 2>&1; then
    echo "Missing required command: $1" >&2
    exit 1
  fi
}

need docker
need zip
need shasum

if [[ "$REFRESH_DB" = "1" ]]; then
  echo "Refreshing MySQL dump from local Docker container..."
  ./deploy/scripts/export-local-data.sh
fi

if [[ "$REFRESH_FRONTEND" = "1" ]]; then
  echo "Refreshing frontend static assets from local nginx directory..."
  ./deploy/scripts/sync-local-nginx-assets.sh
fi

if [[ "$SKIP_MAVEN" = "1" ]]; then
  if [[ ! -f ledger-admin/target/ledger-admin.jar ]]; then
    echo "SKIP_MAVEN=1 but ledger-admin/target/ledger-admin.jar does not exist." >&2
    exit 1
  fi
else
  need mvn
  echo "Building backend jar with Maven..."
  mvn -B -DskipTests clean package
fi

echo "Preparing Docker contexts..."
cp ledger-admin/target/ledger-admin.jar deploy/docker/app/ledger-admin.jar

echo "Ensuring buildx builder is available..."
docker buildx inspect ledger-amd64-builder >/dev/null 2>&1 \
  || docker buildx create --name ledger-amd64-builder --use
docker buildx use ledger-amd64-builder
docker buildx inspect --bootstrap >/dev/null

echo "Building linux/amd64 images on local machine. This may be slow on Apple Silicon..."
docker buildx build --platform linux/amd64 --build-arg "DOCKER_MIRROR=${DOCKER_MIRROR}" --load -t "ledger-app:${IMAGE_TAG}" deploy/docker/app
docker buildx build --platform linux/amd64 --build-arg "DOCKER_MIRROR=${DOCKER_MIRROR}" --load -t "ledger-mysql:${IMAGE_TAG}" deploy/docker/mysql
docker buildx build --platform linux/amd64 --build-arg "DOCKER_MIRROR=${DOCKER_MIRROR}" --load -t "ledger-nginx:${IMAGE_TAG}" deploy/docker/nginx
docker pull --platform linux/amd64 "$REDIS_SOURCE_IMAGE"
docker tag "$REDIS_SOURCE_IMAGE" redis:7.2-alpine

echo "Verifying image platforms before docker save..."
for image in "ledger-app:${IMAGE_TAG}" "ledger-mysql:${IMAGE_TAG}" "ledger-nginx:${IMAGE_TAG}" "redis:7.2-alpine"; do
  platform="$(docker image inspect "$image" --format '{{.Os}}/{{.Architecture}}')"
  echo "$image -> $platform"
  if [[ "$platform" != "linux/amd64" ]]; then
    echo "Refusing to package $image because platform is $platform, expected linux/amd64." >&2
    exit 1
  fi
done

echo "Packaging Windows Intel deployment bundle..."
rm -rf "$DIST_DIR"
mkdir -p "$DIST_DIR"
cp -R deploy/windows-intel/. "$DIST_DIR/"
cp deploy/README-windows-intel.md "$DIST_DIR/README.md"
sed -i.bak "s/^LEDGER_IMAGE_TAG=.*/LEDGER_IMAGE_TAG=${IMAGE_TAG}/" "$DIST_DIR/.env"
rm -f "$DIST_DIR/.env.bak"

docker save \
  -o "$DIST_DIR/ledger-images-linux-amd64.tar" \
  "ledger-app:${IMAGE_TAG}" \
  "ledger-mysql:${IMAGE_TAG}" \
  "ledger-nginx:${IMAGE_TAG}" \
  "redis:7.2-alpine"

if [[ ! -s "$DIST_DIR/ledger-images-linux-amd64.tar" ]]; then
  echo "Docker image tar was not created: $DIST_DIR/ledger-images-linux-amd64.tar" >&2
  exit 1
fi

(
  cd "$ROOT_DIR/dist"
  rm -f "$ZIP_NAME" "$ZIP_NAME.sha256"
  zip -r "$ZIP_NAME" ledger-windows-intel
  unzip -l "$ZIP_NAME" ledger-windows-intel/ledger-images-linux-amd64.tar >/dev/null
  shasum -a 256 "$ZIP_NAME" > "$ZIP_NAME.sha256"
  shasum -a 256 -c "$ZIP_NAME.sha256"
  if [[ "$SKIP_ZIP_TEST" != "1" ]]; then
    unzip -t "$ZIP_NAME" >/dev/null
  fi
  ls -lh "$ZIP_NAME" "$ZIP_NAME.sha256" "ledger-windows-intel/ledger-images-linux-amd64.tar"
)

echo "Done:"
echo "  $ROOT_DIR/dist/$ZIP_NAME"
echo "  $ROOT_DIR/dist/$ZIP_NAME.sha256"
