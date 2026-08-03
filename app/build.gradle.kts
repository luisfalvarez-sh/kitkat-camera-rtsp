plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.digitalservices.cooau"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.digitalservices.cooau"
        minSdk = 19
        targetSdk = 19
        versionCode = 1
        versionName = "1.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {
    // Para Android 4.4 KitKat nativo puro, no se requieren dependencias externas
}
