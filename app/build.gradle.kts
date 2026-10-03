plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.devtools.ksp")
    id("org.jetbrains.kotlin.plugin.compose")
}

android {
    namespace = "com.kurupdevs.woodnest"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kurupdevs.woodnest"
        minSdk = 26
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
    }

    // Release signing is optional: when keystore.properties exists (local dev machine),
    // the release build is signed. In CI (no keystore) it builds unsigned and gets
    // signed afterwards with apksigner. (Pure-Kotlin parse: java.* is not
    // resolvable inside the android{} DSL scope.)
    val keystoreProps: Map<String, String> = run {
        val f = rootProject.file("keystore.properties")
        if (!f.exists()) emptyMap()
        else f.readLines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && "=" in it }
            .associate { line ->
                val idx = line.indexOf("=")
                line.substring(0, idx).trim() to line.substring(idx + 1).trim()
            }
    }
    val hasKeystore = keystoreProps.isNotEmpty()

    signingConfigs {
        if (hasKeystore) {
            create("release") {
                storeFile = file(keystoreProps.getValue("storeFile"))
                storePassword = keystoreProps.getValue("storePassword")
                keyAlias = keystoreProps.getValue("keyAlias")
                keyPassword = keystoreProps.getValue("keyPassword")
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = false // drawables resolved via getIdentifier must not be stripped
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            if (hasKeystore) signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
    buildFeatures {
        compose = true
    }
}

dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.09.00")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.activity:activity-compose:1.9.3")
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-graphics")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.navigation:navigation-compose:2.7.7")
    implementation("androidx.lifecycle:lifecycle-runtime-compose:2.8.6")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6")
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")
}

// CI fix: the "WoodNest Release APK" workflow uploads
// app/build/outputs/apk/release/app-release.apk, but an unsigned release
// build is emitted as app-release-unsigned.apk. Copy it to the expected name
// right after assembleRelease so the upload step finds it. (Plain Gradle
// task API only: AGP 8's VariantOutput interface has no outputFileName
// property, and tasks.named() is lazy so it works even though AGP creates
// assembleRelease after this script is evaluated.)
val copyUnsignedApk by tasks.registering(Copy::class) {
    from(layout.buildDirectory.file("outputs/apk/release/app-release-unsigned.apk"))
    into(layout.buildDirectory.dir("outputs/apk/release"))
    rename { "app-release.apk" }
}

tasks.named("assembleRelease") {
    finalizedBy(copyUnsignedApk)
}
