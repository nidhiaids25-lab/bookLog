#!/usr/bin/env bash
# ==============================================================================
# Smart Library System - Build & Run Script (Linux / macOS)
# ==============================================================================

set -e

PROJECT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
cd "$PROJECT_DIR"

echo "=================================================================="
echo "          📚 SMART LIBRARY SYSTEM - LAUNCHER                      "
echo "=================================================================="

# Create bin output directory
mkdir -p bin

echo "🔨 Compiling Java sources..."
javac -cp "lib/*" -d bin $(find src -name "*.java")

echo "✅ Compilation successful!"
echo "🚀 Starting Smart Library System (Server + Web UI)..."
echo ""

# Run Main class with classpath including bin and all jars in lib
java -cp "bin:lib/*" Main "$@"
