#!/usr/bin/env bash
# ============================================================================
# G4IMS Server - Environment Variable Registration (Linux/macOS)
# ============================================================================
# This script copies the env file to ~/.config/g4ims/ and adds a source line
# to your shell RC file (.bashrc or .zshrc).
#
# Usage: bash register-server-env-linux.sh
# ============================================================================

set -e

SCRIPT_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
CONFIG_DIR="$HOME/.config/g4ims"
ENV_FILE="$CONFIG_DIR/g4ims-server.env"

echo "=== G4IMS Server Environment Registration ==="
echo ""

# Create config directory
mkdir -p "$CONFIG_DIR"

# Copy env example
if [ ! -f "$ENV_FILE" ]; then
  cp "$SCRIPT_DIR/g4ims-server.env.example" "$ENV_FILE"
  echo "[OK] Created $ENV_FILE"
  echo "     IMPORTANT: Edit this file and replace default passwords/secrets!"
else
  echo "[SKIP] $ENV_FILE already exists."
  echo "       To reset, delete it and re-run this script."
fi

# Detect shell
SHELL_NAME="$(basename "$SHELL")"
case "$SHELL_NAME" in
  zsh)  RC_FILE="$HOME/.zshrc" ;;
  bash) RC_FILE="$HOME/.bashrc" ;;
  *)    RC_FILE="$HOME/.profile" ;;
esac

# Add source line to RC file
SOURCE_LINE="source \"$ENV_FILE\""
if grep -qF "$SOURCE_LINE" "$RC_FILE" 2>/dev/null; then
  echo "[SKIP] Source line already present in $RC_FILE"
else
  echo "" >> "$RC_FILE"
  echo "# G4IMS Server environment variables" >> "$RC_FILE"
  echo "$SOURCE_LINE" >> "$RC_FILE"
  echo "[OK] Added source line to $RC_FILE"
fi

echo ""
echo "=== Done! ==="
echo "Restart your terminal or run: source $ENV_FILE"
echo ""
echo "SECURITY REMINDER:"
echo "  - Replace G4IMS_SERVER_DB_PASSWORD with a real password"
echo "  - Replace G4IMS_SERVER_AUTH_TOKEN_SECRET with a 32+ char random string"
echo "  - Replace G4IMS_SERVER_DEFAULT_ADMIN_PASSWORD before production"
