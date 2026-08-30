#!/usr/bin/env bash

set -euo pipefail

plugin_name="sleepwalker-stakeout"
dry_run=false

script_dir="$(cd -- "$(dirname -- "${BASH_SOURCE[0]}")" && pwd)"
plugin_hub_dir="${PLUGIN_HUB_DIR:-"$script_dir/../plugin-hub"}"

confirm() {
  local response

  read -r -p "$1 [Y/n]: " response

  [[ "$response" =~ ^([Yy]([Ee][Ss])?)?$ ]]
}

usage() {
  echo "Usage: $0 [--dry-run]"
  echo
  echo "Options:"
  echo "  --dry-run    Preview the Plugin Hub update without modifying anything"
  echo "  -h, --help   Show this help message"
}

while [[ $# -gt 0 ]]; do
  case "$1" in
    --dry-run)
      dry_run=true
      shift
      ;;
    -h|--help)
      usage
      exit 0
      ;;
    *)
      echo "Error: unknown option: $1" >&2
      usage
      exit 1
      ;;
  esac
done

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

if [[ ! -d "$plugin_hub_dir/.git" ]]; then
  echo "Error: Plugin Hub repository not found at:" >&2
  echo "  $plugin_hub_dir" >&2
  echo >&2
  echo "Set PLUGIN_HUB_DIR to override the location." >&2
  exit 1
fi

echo
echo "Plugin:  $plugin_name"
echo "Version: $gradle_version"
echo "Commit:  $commit_hash"

if $dry_run; then
  echo "Mode:    dry run"
fi

echo

cd "$plugin_hub_dir"

if ! git remote get-url upstream &>/dev/null; then
  echo "Error: Plugin Hub repository has no 'upstream' remote." >&2
  echo >&2
  echo "Expected remotes:" >&2
  echo "  origin   -> your Plugin Hub fork" >&2
  echo "  upstream -> https://github.com/runelite/plugin-hub.git" >&2
  exit 1
fi

plugin_file="plugins/$plugin_name"

git fetch upstream

if ! git cat-file -e "upstream/master:$plugin_file" 2>/dev/null; then
  echo "Error: Plugin Hub entry not found:" >&2
  echo "  $plugin_file" >&2
  exit 1
fi

if $dry_run; then
  temp_dir="$(mktemp -d)"
  trap 'rm -rf "$temp_dir"' EXIT

  mkdir -p \
    "$temp_dir/a/$(dirname "$plugin_file")" \
    "$temp_dir/b/$(dirname "$plugin_file")"

  git show "upstream/master:$plugin_file" \
    > "$temp_dir/a/$plugin_file"

  sed \
    "s/^commit=[0-9a-f]*/commit=$commit_hash/" \
    "$temp_dir/a/$plugin_file" \
    > "$temp_dir/b/$plugin_file"

  echo "Proposed Plugin Hub changes:"
  echo

  set +e
  (
    cd "$temp_dir"
    git diff --no-index --no-prefix -- \
      "a/$plugin_file" \
      "b/$plugin_file"
  )
  diff_status=$?
  set -e

  if [[ $diff_status -gt 1 ]]; then
    echo "Error: failed to generate Plugin Hub diff" >&2
    exit "$diff_status"
  fi

  echo
  echo "Commit message:"
  echo "  update $plugin_name to $gradle_version"
  echo

  if [[ $diff_status -eq 0 ]]; then
    echo "No Plugin Hub changes are required."
  else
    echo "Dry run complete. No files were modified, committed, or pushed."
  fi

  exit 0
fi

if [[ -n "$(git status --porcelain)" ]]; then
  echo "Error: Plugin Hub working tree has uncommitted changes." >&2
  exit 1
fi

if ! git remote get-url origin &>/dev/null; then
  echo "Error: Plugin Hub repository has no 'origin' remote." >&2
  echo >&2
  echo "Expected origin to point to your Plugin Hub fork." >&2
  exit 1
fi

if ! confirm "Continue with Plugin Hub update?"; then
  exit 0
fi

git checkout -B "$plugin_name" upstream/master

temp_file="$(mktemp)"
trap 'rm -f "$temp_file"' EXIT

sed \
  "s/^commit=[0-9a-f]*/commit=$commit_hash/" \
  "$plugin_file" \
  > "$temp_file"

mv "$temp_file" "$plugin_file"

commit_message="update $plugin_name to $gradle_version"

echo
echo "Changes:"
echo

git diff -- "$plugin_file"

if git diff --quiet -- "$plugin_file"; then
  echo "No Plugin Hub changes are required."
  exit 0
fi

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