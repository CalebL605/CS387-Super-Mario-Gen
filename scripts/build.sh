#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

if ! command -v javac >/dev/null 2>&1; then
    printf 'javac was not found. Install JDK 25 or newer and add its bin directory to PATH.\n' >&2
    exit 1
fi

sources=()
while IFS= read -r -d '' source; do
    sources+=("$source")
done < <(find src -type f -name '*.java' -print0)

if [ "${#sources[@]}" -eq 0 ]; then
    printf 'No Java source files were found in src/.\n' >&2
    exit 1
fi

mkdir -p out
javac --release 25 -d out "${sources[@]}"
printf 'Compiled %s Java source files into out/.\n' "${#sources[@]}"
