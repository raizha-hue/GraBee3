plugins {
    id("com.android.application")
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.grabee"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.grabee"
        minSdk = 23
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        // Add these to access from BuildConfig
        buildConfigField("String", "SUPABASE_URL", "\"${properties["supabase.url"]}\"")
        buildConfigField("String", "SUPABASE_KEY", "\"${properties["supabase.key"]}\"")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.3"
    }
}

dependencies {
    // Compose BOM
    val composeBom = platform("androidx.compose:compose-bom:2023.10.01")
    implementation(composeBom)
    androidTestImplementation(composeBom)

    // Core Compose
    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.ui:ui-tooling-preview")
    implementation("androidx.compose.foundation:foundation")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.material:material-icons-extended")
    implementation("androidx.activity:activity-compose:1.8.0")
    implementation("androidx.navigation:navigation-compose:2.7.7")

    // AndroidX Core
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.2")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.2")

    // Firebase
    implementation(platform("com.google.firebase:firebase-bom:32.7.0"))
    implementation("com.google.firebase:firebase-auth-ktx")
    implementation("com.google.firebase:firebase-firestore-ktx")
    implementation("com.google.firebase:firebase-storage-ktx")
    implementation("com.google.firebase:firebase-messaging-ktx")
    implementation("com.google.firebase:firebase-common-ktx:20.4.2")

    // Image Loading
    implementation("io.coil-kt:coil-compose:2.4.0")

    // Coroutines
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.7.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-play-services:1.7.3")

    // Testing
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")

    // Debug Tools
    debugImplementation("androidx.compose.ui:ui-tooling")
    debugImplementation("androidx.compose.ui:ui-test-manifest")

    // Supabase - Use version 1.4.1 which is stable and available in Maven Central
    implementation("io.github.jan.supabase:postgrest-kt:1.4.1") {
        exclude(group = "io.ktor", module = "ktor-client-core")
    }
    implementation("io.github.jan.supabase:storage-kt:1.4.1") {
        exclude(group = "io.ktor", module = "ktor-client-core")
    }
    implementation("io.ktor:ktor-client-okhttp:2.3.5") // Explicit ktor client

    // Supabase
    implementation("io.github.jan.supabase:postgrest-kt:1.4.1") // For database
    implementation("io.github.jan.supabase:storage-kt:1.4.1")   // For file storage
    implementation("io.ktor:ktor-client-okhttp:2.3.5")         // HTTP client

    // Supabase
    implementation("io.github.jan.supabase:postgrest-kt:1.4.1") // Database
    implementation("io.github.jan.supabase:gotrue-kt:1.4.1")    // Auth
    implementation("io.github.jan.supabase:storage-kt:1.4.1")   // Storage
    implementation("io.github.jan.supabase:realtime-kt:1.4.1")  // Realtime

    // Required dependencies
    implementation("io.ktor:ktor-client-okhttp:2.3.5")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")


}