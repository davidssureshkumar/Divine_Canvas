# Divine Canvas

Create visually stunning Bible-verse images formatted for WhatsApp Status (9:16) —
**100% free, no paywalls, no ads, no login required.**

Built with Kotlin · Jetpack Compose · MVVM + Clean Architecture · Hilt · Room ·
Retrofit · Coil.

---

## Features

- **Dual verse selection**
  - *Manual*: three reactive Exposed Dropdowns — **Book** (grouped into Old / New
    Testament) → **Chapter** (populates from the book) → **Verse** (populates the
    exact verse count for that chapter). Powered entirely offline by a bundled
    versification table in Room.
  - *Theme*: pick a theme (Faith, Anxiety, Joy, Strength, …) and auto-pick a
    relevant verse.
  - Translation selector — **KJV (fully offline)**, WEB, ASV free; NIV/NKJV/ESV
    available via your own licensed key (see *Translations & licensing* below).
- **9:16 WYSIWYG canvas** with a dedicated off-screen render node so every export is
  a fixed **1080×1920** full-resolution image, independent of the preview size.
- **Restores your last session** — the verse and full styling are persisted and
  reloaded on next launch.
- **Backgrounds** — 10 built-in gradients (always offline) + optional Unsplash/Pexels
  photo search.
- **Typography** — font family, size, alignment, color palette, text shadow, and an
  adjustable readability overlay scrim.
- **Signature banner** — optional watermark anchored Bottom / Top / Left / Right
  (left & right are rotated vertical). Leave the text empty and it occupies **0dp**
  and is not rendered at all.
- **Export** — Save to Gallery (MediaStore / scoped storage) and Share to WhatsApp
  (falls back to the system share sheet if WhatsApp isn't installed).
- **Optional Google Sign-In** via Credential Manager — purely opt-in; every feature
  works signed-out.

## Project structure

```
app/src/main/java/com/divinecanvas/
├── core/                 # AppResult and small helpers
├── data/
│   ├── auth/             # GoogleAuthManager (Credential Manager)
│   ├── local/            # Room DB, DAO, entities, JSON seeder
│   ├── prefs/            # DataStore (optional account info)
│   ├── remote/           # Retrofit APIs + DTOs (bible-api, Unsplash, Pexels)
│   └── repository/       # Repository implementations
├── di/                   # Hilt modules (App, Database, Network, Repository)
├── domain/               # Models + repository interfaces (framework-free)
└── ui/
    ├── canvas/           # VerseCanvas (render) + ImageExporter
    ├── editor/           # Editor screen, ViewModel, components
    ├── settings/         # Settings screen + ViewModel
    ├── navigation/       # NavGraph
    └── theme/            # Material3 theme
app/src/main/assets/bible/
├── versification.json    # 66 books → per-chapter verse counts (offline dropdowns)
└── themes.json           # Curated WEB verses per theme (offline)
```

## Build & run

1. **Open in Android Studio** (Hedgehog or newer). It bundles a compatible **JDK 17**
   — AGP 8.5 requires JDK 17 (the `java` on your PATH may be older; that's fine,
   Studio uses its own).
2. Let Gradle sync. The first sync creates `local.properties` with your `sdk.dir`.
3. Run the `app` configuration on a device/emulator (minSdk 24).

CLI (with JDK 17 active):

```bash
./gradlew :app:assembleDebug      # build debug APK
./gradlew :app:testDebugUnitTest  # run unit tests
./gradlew :app:lintDebug          # lint
./gradlew :app:assembleRelease    # minified + shrunk release build
```

### Optional API keys

Copy `local.properties.example` → `local.properties` and fill in any you have.
**All are optional** — with none set, the app runs fully offline (bundled verses +
gradient backgrounds, no sign-in). See that file for where to get free keys.

## Translations & licensing

- **KJV** — complete public-domain text is bundled in `assets/bible/kjv.json` and
  served by `KjvOfflineSource`, so KJV works **fully offline, no network ever**.
- **WEB / ASV** — public domain; fetched from the free key-less
  [bible-api.com](https://bible-api.com) and cached for offline reuse.
- **NIV / NKJV / ESV** — these are **copyrighted** and legally **cannot** be bundled
  or served for free. They are modeled as `LICENSED` and route through *your own*
  [scripture.api.bible](https://scripture.api.bible) key (`API_BIBLE_KEY` +
  `API_BIBLE_ID_*` in `local.properties`). Without a key they stay disabled and the
  app explains why. The app ships **no copyrighted scripture text**.

## Offline data note

`assets/bible/versification.json` holds the standard Protestant (KJV) verse counts
so the Book/Chapter/Verse dropdowns work with zero network. These counts are
**verified by a unit test** (`KjvVersificationIntegrityTest`) that cross-checks them
against the actual verse counts in the bundled `kjv.json` text — so the dropdowns
can never offer a verse the offline KJV doesn't have. The KJV text itself uses the
standard, verse-numbered public-domain dataset.

## Fonts

Playfair Display (`res/font/playfair_display.ttf`) is included under the SIL Open
Font License; the license is bundled at `assets/licenses/PlayfairDisplay-OFL.txt`.

## Play Store readiness

- `proguard-rules.pro` — R8/obfuscation rules for serialization, Retrofit, Room,
  Hilt, Coil, and Credential Manager. Release build sets `isMinifyEnabled` and
  `isShrinkResources`.
- All user-facing copy lives in `res/values/strings.xml` for localization.
- Permissions are minimal: `INTERNET`, `ACCESS_NETWORK_STATE`, and
  `WRITE_EXTERNAL_STORAGE` only on API ≤ 28.
- Privacy Policy / Terms links surface in **Settings → About** (configurable URLs).
- Targets **API 35** (`compileSdk`/`targetSdk`) to meet Play's target-API requirement.
- **App Bundle:** the `Release AAB` workflow (`.github/workflows/release.yml`) builds
  a `.aab` via `:app:bundleRelease`. Release signing reads a real upload key from CI
  secrets (`KEYSTORE_BASE64`, `ANDROID_KEYSTORE_PASSWORD`, `ANDROID_KEY_ALIAS`,
  `ANDROID_KEY_PASSWORD`) or a local `keystore.properties`; without one it falls back
  to debug signing so the build still succeeds (but isn't uploadable).
- **Before publishing:** generate your upload key, add the four secrets above, enroll
  in Play App Signing, and fill in the Data Safety form + a real Privacy Policy URL.

## Tests

- **JVM unit tests** (`app/src/test/...`): offline versification logic, the reactive
  dropdown state machine, the banner conditional-rendering rule, the repository's
  cache-first / network-fallback behavior, and a versification↔KJV integrity check.
- **Instrumented tests** (`app/src/androidTest/...`): run on a real Android emulator
  in CI (`.github/workflows/instrumented.yml`) — an app-launch smoke test and an
  on-device `KjvOfflineSource` test. These catch Android-runtime issues that JVM
  tests can't (e.g. the ICU regex engine, Hilt graph wiring).
