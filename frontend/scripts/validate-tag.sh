#!/usr/bin/env bash
set -euo pipefail

TAG="${1:-}"

if [[ -z "$TAG" ]]; then
  echo "::error::No tag supplied to validate-tag.sh" >&2
  exit 1
fi

SEMVER_REGEX='^v[0-9]+\.[0-9]+\.[0-9]+(-[0-9A-Za-z.-]+)?(\+[0-9A-Za-z.-]+)?$'

if [[ ! "$TAG" =~ $SEMVER_REGEX ]]; then
  echo "::error::Tag '$TAG' is not valid semver. Expected e.g. v1.2.3 or v1.2.3-rc.1 (must start with 'v')." >&2
  exit 1
fi

echo "Tag '$TAG' is valid semver."
