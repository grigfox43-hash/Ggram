#!/bin/sh

# Gradle startup script for POSIX systems

DIRNAME="$(dirname "$0")"
APP_BASE_NAME="$(basename "$0")"
APP_HOME="$(cd "$DIRNAME" && pwd)"

# Run gradle or fallback
if command -v gradle >/dev/null 2>&1; then
    exec gradle "$@"
else
    echo "Gradle not found in PATH" >&2
    exit 1
fi
