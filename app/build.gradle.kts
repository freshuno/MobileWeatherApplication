import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "pl.example.weatherforecastapp"
    compileSdk = 34

    // 1. Włączamy funkcję buildConfig
    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "pl.example.weatherforecastapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        // 2. Wczytujemy klucz z pliku local.properties
        val keystoreFile = project.rootProject.file("local.properties")
        val properties = Properties()
        if (keystoreFile.exists()) {
            properties.load(keystoreFile.inputStream())
        }

        // 3. Pobieramy klucz (upewnij się, że w local.properties jest wpisany z cudzysłowem, np. OPEN_WEATHER_API_KEY="twoj_klucz")
        val apiKey = properties.getProperty("OPEN_WEATHER_API_KEY") ?: ""

        // 4. Generujemy pole w klasie BuildConfig
        buildConfigField("String", "OPEN_WEATHER_API_KEY", apiKey)
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation("com.android.volley:volley:1.2.1")
    implementation("com.google.mlkit:entity-extraction:16.0.0-beta3")
}