# Firebase Studio (Project IDX) environment for Divine Canvas.
# Builds/tests the app on Google's cloud machine — nothing installed locally.
# Docs: https://firebase.google.com/docs/studio/customize-workspace
{ pkgs, ... }: {
  channel = "stable-24.05";

  packages = [
    pkgs.jdk17
    pkgs.unzip
    pkgs.wget
  ];

  env = {
    ANDROID_HOME = "/home/user/androidsdk";
    ANDROID_SDK_ROOT = "/home/user/androidsdk";
  };

  idx = {
    extensions = [
      "redhat.java"
      "vscjava.vscode-gradle"
      "fwcd.kotlin"
    ];

    workspace = {
      # One-time setup: install the Android command-line SDK in the cloud workspace.
      onCreate = {
        install-android-sdk = ''
          set -e
          SDK="$ANDROID_HOME"
          mkdir -p "$SDK/cmdline-tools"
          if [ ! -d "$SDK/cmdline-tools/latest" ]; then
            wget -q https://dl.google.com/android/repository/commandlinetools-linux-11076708_latest.zip -O /tmp/cmdtools.zip
            unzip -q /tmp/cmdtools.zip -d "$SDK/cmdline-tools"
            mv "$SDK/cmdline-tools/cmdline-tools" "$SDK/cmdline-tools/latest"
          fi
          yes | "$SDK/cmdline-tools/latest/bin/sdkmanager" --licenses >/dev/null 2>&1 || true
          "$SDK/cmdline-tools/latest/bin/sdkmanager" \
            "platform-tools" "platforms;android-34" "build-tools;34.0.0" >/dev/null
        '';
        default.openFiles = [
          "README.md"
          "app/src/main/java/com/divinecanvas/ui/editor/EditorScreen.kt"
        ];
      };
    };

    # NOTE: Firebase Studio's live device preview is first-class for Flutter/web.
    # For this native-Kotlin app, build the APK here (./gradlew :app:assembleDebug),
    # then run it via Firebase Test Lab / Appetize.io, or download + sideload it.
  };
}
