plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.fetchin.kiosk"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.fetchin.kiosk"
        minSdk = 26
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"

        buildConfigField("String", "DEFAULT_START_URL", "\"https://pos.example.com\"")
        buildConfigField("String", "DEFAULT_ALLOWED_HOSTS", "\"pos.example.com,api.pos.example.com,sub.pos.example.com\"")
        buildConfigField("String", "DEFAULT_ADMIN_PIN_HASH_BASE64", "\"\"")
        buildConfigField("String", "DEFAULT_ADMIN_PIN_SALT_BASE64", "\"\"")
        buildConfigField("int", "DEFAULT_ADMIN_PIN_ITERATIONS", "210000")
        buildConfigField("int", "DEFAULT_ADMIN_PIN_KEY_LENGTH_BITS", "256")
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isDebuggable = true
            buildConfigField("boolean", "WEBVIEW_DEBUGGING", "true")
        }
        release {
            isMinifyEnabled = false
            isShrinkResources = false
            isDebuggable = false
            buildConfigField("boolean", "WEBVIEW_DEBUGGING", "false")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }

    buildFeatures {
        viewBinding = true
        buildConfig = true
    }

    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.material)

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.espresso.core)
}
