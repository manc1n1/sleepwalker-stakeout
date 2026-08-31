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
plugin_file="src/main/java/com/sleepwalkerstakeout/SleepwalkerStakeoutPlugin.java"

for file in \
  "$gradle_properties" \
  "$runelite_properties" \
  "$plugin_file"
do
  if [[ ! -f "$file" ]]; then
    echo "Error: $file not found" >&2
    exit 1
  fi
done

current_gradle_version="$(
  sed -n 's/^plugin_version=//p' "$gradle_properties"
)"

current_runelite_version="$(
  sed -n 's/^version=//p' "$runelite_properties"
)"

current_plugin_version="$(
  sed -nE \
    's/^[[:space:]]*private static final String PLUGIN_VERSION = "([^"]+)";/\1/p' \
    "$plugin_file"
)"

if [[ -z "$current_gradle_version" ]]; then
  echo "Error: plugin_version not found in $gradle_properties" >&2
  exit 1
fi

if [[ -z "$current_runelite_version" ]]; then
  echo "Error: version not found in $runelite_properties" >&2
  exit 1
fi

if [[ -z "$current_plugin_version" ]]; then
  echo "Error: PLUGIN_VERSION not found in $plugin_file" >&2
  exit 1
fi

if [[ "$current_gradle_version" == "$version" \
   && "$current_runelite_version" == "$version" \
   && "$current_plugin_version" == "$version" ]]; then
  echo "Version is already $version in all files"
  exit 0
fi

replace_line() {
  local file="$1"
  local expression="$2"
  local temp_file

  temp_file="$(mktemp)"

  sed -E "$expression" "$file" > "$temp_file"
  mv "$temp_file" "$file"
}

replace_line \
  "$gradle_properties" \
  "s|^plugin_version=.*|plugin_version=$version|"

replace_line \
  "$runelite_properties" \
  "s|^version=.*|version=$version|"

replace_line \
  "$plugin_file" \
  "s|^([[:space:]]*private static final String PLUGIN_VERSION = \")[^\"]+(\";)|\1${version}\2|"

echo "Version bumped:"
echo "  $current_gradle_version -> $version"
echo
echo "Updated:"
echo "  $gradle_properties"
echo "  $runelite_properties"
echo "  $plugin_file"