#!/usr/bin/env bash
# Installs the Android command-line SDK into a Codespaces/dev container.
# Runs once on container creation. Standard Ubuntu/Debian => SDK binaries
# (aapt2/d8) run without the Nix linker issues you can hit elsewhere.
set -euo pipefail

SDK="${ANDROID_SDK_ROOT:-/usr/local/android-sdk}"
TOOLS_URL="https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip"

# Ensure unzip is available.
if ! command -v unzip >/dev/null 2>&1; then
  sudo apt-get update -y && sudo apt-get install -y unzip
fi

sudo mkdir -p "$SDK/cmdline-tools"
sudo chown -R "$(whoami)" "$SDK"

if [ ! -d "$SDK/cmdline-tools/latest" ]; then
  tmp="$(mktemp -d)"
  echo "Downloading Android command-line tools..."
  curl -fsSL "$TOOLS_URL" -o "$tmp/tools.zip"
  unzip -q "$tmp/tools.zip" -d "$tmp"
  mv "$tmp/cmdline-tools" "$SDK/cmdline-tools/latest"
  rm -rf "$tmp"
fi

SDKMANAGER="$SDK/cmdline-tools/latest/bin/sdkmanager"
yes | "$SDKMANAGER" --licenses >/dev/null 2>&1 || true
"$SDKMANAGER" "platform-tools" "platforms;android-34" "build-tools;34.0.0"

echo "Android SDK ready at $SDK"
echo "Build with:  ./gradlew :app:assembleDebug"
