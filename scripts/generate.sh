#!/usr/bin/env bash
set -euo pipefail

repo_root="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$repo_root"

if ! command -v java >/dev/null 2>&1; then
    printf 'java was not found. Install JDK 25 or newer and add its bin directory to PATH.\n' >&2
    exit 1
fi

if [ ! -f out/levelGenerators/npclrf/Generate.class ]; then
    printf 'Generator CLI not found. Add levelGenerators.npclrf.Generate and run bash scripts/build.sh.\n' >&2
    exit 1
fi

exec java -cp out levelGenerators.npclrf.Generate "$@"
