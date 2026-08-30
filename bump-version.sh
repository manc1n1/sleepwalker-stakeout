#!/usr/bin/env bash

set -euo pipefail

if [[ $# -ne 1 ]]; then
  echo "Usage: $0 <version>"
  echo "Example: $0 1.1.0"
  exit 1
fi

version="$1"

if [[ ! "$version" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
  echo "Error: version must use semantic versioning (e.g. 1.1.0)" >&2
  exit 1
fi

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
cd "$script_dir"

gradle_properties="gradle.properties"
runelite_properties="runelite-plugin.properties"

if [[ ! -f "$gradle_properties" ]]; then
  echo "Error: $gradle_properties not found" >&2
  exit 1
fi

if [[ ! -f "$runelite_properties" ]]; then
  echo "Error: $runelite_properties not found" >&2
  exit 1
fi

current_version="$(sed -n 's/^plugin_version=//p' "$gradle_properties")"

if [[ -z "$current_version" ]]; then
  echo "Error: plugin_version not found in $gradle_properties" >&2
  exit 1
fi

if [[ "$current_version" == "$version" ]]; then
  echo "Version is already $version"
  exit 0
fi

replace_version() {
  local file="$1"
  local pattern="$2"
  local replacement="$3"
  local temp_file

  temp_file="$(mktemp)"

  sed "s/^${pattern}.*/${replacement}/" "$file" > "$temp_file"
  mv "$temp_file" "$file"
}

replace_version \
  "$gradle_properties" \
  "plugin_version=" \
  "plugin_version=$version"

replace_version \
  "$runelite_properties" \
  "version=" \
  "version=$version"

echo "Version bumped:"
echo "  $current_version -> $version"
echo
echo "Updated:"
echo "  $gradle_properties"
echo "  $runelite_properties"