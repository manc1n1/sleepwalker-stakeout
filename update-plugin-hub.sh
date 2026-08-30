#!/usr/bin/env bash

set -euo pipefail

plugin_name="sleepwalker-stakeout"

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
plugin_hub_dir="${PLUGIN_HUB_DIR:-"$script_dir/../plugin-hub"}"

confirm() {
  local response

  read -r -p "$1 [Y/n]: " response

  [[ "$response" =~ ^([Yy]([Ee][Ss])?)?$ ]]
}

cd "$script_dir"

commit_hash="$(git rev-parse HEAD)"
gradle_version="$(sed -n 's/^plugin_version=//p' gradle.properties)"
runelite_version="$(sed -n 's/^version=//p' runelite-plugin.properties)"

if [[ -z "$gradle_version" ]]; then
  echo "Error: plugin_version is missing from gradle.properties" >&2
  exit 1
fi

if [[ -z "$runelite_version" ]]; then
  echo "Error: version is missing from runelite-plugin.properties" >&2
  exit 1
fi

if [[ "$gradle_version" != "$runelite_version" ]]; then
  echo "Error: plugin versions do not match:" >&2
  echo "  gradle.properties:          $gradle_version" >&2
  echo "  runelite-plugin.properties: $runelite_version" >&2
  exit 1
fi

echo
echo "Plugin:  $plugin_name"
echo "Version: $gradle_version"
echo "Commit:  $commit_hash"
echo

if ! confirm "Continue with Plugin Hub update?"; then
  exit 0
fi

if [[ ! -d "$plugin_hub_dir/.git" ]]; then
  echo "Error: Plugin Hub repository not found at:" >&2
  echo "  $plugin_hub_dir" >&2
  echo >&2
  echo "Set PLUGIN_HUB_DIR to override the location." >&2
  exit 1
fi

cd "$plugin_hub_dir"

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Error: Plugin Hub working tree has uncommitted changes." >&2
  exit 1
fi

plugin_file="plugins/$plugin_name"

if [[ ! -f "$plugin_file" ]]; then
  echo "Error: Plugin Hub entry not found:" >&2
  echo "  $plugin_file" >&2
  exit 1
fi

echo "Updating Plugin Hub repository..."

git fetch upstream
git checkout -B "$plugin_name" upstream/master

temp_file="$(mktemp)"
trap 'rm -f "$temp_file"' EXIT

sed \
  "s/^commit=[0-9a-f]*/commit=$commit_hash/" \
  "$plugin_file" > "$temp_file"

mv "$temp_file" "$plugin_file"

commit_message="update $plugin_name to $gradle_version"

echo
echo "Changes:"
echo

git diff -- "$plugin_file"

echo
echo "Commit message:"
echo "  $commit_message"
echo
echo "Branch:"
echo "  $plugin_name"
echo

if ! confirm "Push this Plugin Hub update?"; then
  exit 0
fi

git add "$plugin_file"
git commit -m "$commit_message"
git push --force-with-lease -u origin "$plugin_name"

echo
echo "Plugin Hub branch pushed successfully."