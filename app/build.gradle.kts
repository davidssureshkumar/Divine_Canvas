import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.gradle.play.publisher)
    alias(libs.plugins.spotless)
}

// Read optional API keys from local.properties (never committed). All keys are
// optional: the app falls back to bundled offline content when they are absent.
val localProps =
    Properties().apply {
        val f = rootProject.file("local.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

fun prop(name: String): String = (localProps.getProperty(name) ?: "").trim()

// Release signing: read from keystore.properties (local) or env vars (CI secrets).
// When no keystore is available the release build falls back to debug signing, so
// `assembleRelease`/`bundleRelease` still succeed for verification (but such an
// artifact is NOT uploadable to Play).
val keystoreProps =
    Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

fun signingProp(
    key: String,
    env: String,
): String? = (keystoreProps.getProperty(key) ?: System.getenv(env))?.takeIf { it.isNotBlank() }

val releaseStoreFilePath = signingProp("storeFile", "ANDROID_KEYSTORE_FILE")
val hasReleaseSigning = releaseStoreFilePath != null && file(releaseStoreFilePath).exists()

android {
    namespace = "com.divinecanvas"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.divinecanvas"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables { useSupportLibrary = true }

        // Optional integration keys — empty string => feature gracefully disabled.
        buildConfigField("String", "UNSPLASH_ACCESS_KEY", "\"${prop("UNSPLASH_ACCESS_KEY")}\"")
        buildConfigField("String", "PEXELS_API_KEY", "\"${prop("PEXELS_API_KEY")}\"")

        // Web client id for optional Google Sign-In via Credential Manager.
        buildConfigField("String", "GOOGLE_WEB_CLIENT_ID", "\"${prop("GOOGLE_WEB_CLIENT_ID")}\"")
        buildConfigField(
            "String",
            "PRIVACY_POLICY_URL",
            "\"${prop("PRIVACY_POLICY_URL").ifEmpty { "https://divinecanvas.app/privacy" }}\""
        )
        buildConfigField(
            "String",
            "TERMS_URL",
            "\"${prop("TERMS_URL").ifEmpty { "https://divinecanvas.app/terms" }}\""
        )
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStoreFilePath!!)
                storePassword = signingProp("storePassword", "ANDROID_KEYSTORE_PASSWORD")
                keyAlias = signingProp("keyAlias", "ANDROID_KEY_ALIAS")
                keyPassword = signingProp("keyPassword", "ANDROID_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            // Real upload key when configured (keystore.properties / CI secrets);
            // otherwise debug signing so the build still succeeds for verification.
            signingConfig =
                if (hasReleaseSigning) {
                    signingConfigs.getByName("release")
                } else {
                    signingConfigs.getByName("debug")
                }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
        freeCompilerArgs += listOf("-Xjvm-default=all")
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    packaging { resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" } }

    testOptions {
        unitTests {
            isReturnDefaultValues = true
            isIncludeAndroidResources = true
        }
    }

    lint {
        warningsAsErrors = false
        abortOnError = false
        checkReleaseBuilds = true
        baseline = file("lint-baseline.xml")
    }
}

// Export Room schemas for migration tracking / CI verification.
ksp { arg("room.schemaLocation", "$projectDir/schemas") }

// Gradle Play Publisher — auto-upload the AAB to Google Play.
// Credentials are resolved from the ANDROID_PUBLISHER_CREDENTIALS env var (CI) or a
// local service-account JSON; no secret is stored in the repo. Publish tasks only
// run when explicitly invoked (e.g. `:app:publishReleaseBundle`), so normal
// build/test workflows are unaffected.
play {
    track.set("internal")
    defaultToAppBundles.set(true)
    // The app must already exist on Play with one manual release before the first
    // automated publish (Google requires the initial upload via the Console).
}

// Code formatting (ktlint via Spotless). Runs only when invoked
// (`:app:spotlessApply` / `:app:spotlessCheck`), so it never blocks normal builds.
spotless {
    kotlin {
        target("src/**/*.kt")
        ktfmt("0.49").kotlinlangStyle()
    }
    kotlinGradle {
        target("*.gradle.kts")
        ktfmt("0.49").kotlinlangStyle()
    }
}

dependencies {
    // Core / lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Networking
    implementation(libs.retrofit.core)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)

    // Images
    implementation(libs.coil.compose)

    // Preferences
    implementation(libs.androidx.datastore.preferences)

    // Optional Google Sign-In (Credential Manager)
    implementation(libs.androidx.credentials)
    implementation(libs.androidx.credentials.play.services.auth)
    implementation(libs.googleid)

    // Runtime permissions helper
    implementation(libs.accompanist.permissions)

    // Unit tests
    testImplementation(libs.junit)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)

    // Instrumented tests
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
