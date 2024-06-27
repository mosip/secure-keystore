plugins {
    id("com.android.application")
    id("kotlin-android")
}

android {
    namespace = "com.example.securekeystoreapp"
    compileSdk= 34
    defaultConfig {
        applicationId = "com.example.securekeystoreapp"
        minSdk=23
        targetSdk= 34
        versionCode = 1
        versionName = "1.0"
    }
    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.4.0"
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    kotlinOptions {
        jvmTarget = "1.8" // Or another valid target version
    }
}

dependencies {
    implementation("org.jetbrains.kotlin:kotlin-stdlib:1.8.0")
    implementation("androidx.core:core-ktx:1.7.0")
    implementation("androidx.appcompat:appcompat:1.4.0")
    implementation("com.google.android.material:material:1.4.0")
    implementation("androidx.biometric:biometric:1.2.0-alpha04") // For biometrics
    implementation("io.mosip:secure-keystore:0.1-SNAPSHOT")
    implementation(libs.androidx.ui.graphics.android)
    implementation(libs.androidx.runtime.android)
    implementation(libs.androidx.foundation.android)
    implementation(libs.androidx.material3.android)

}
