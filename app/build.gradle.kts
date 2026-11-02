
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    alias(libs.plugins.compose.compiler)
}

android {
    namespace = "com.example.expensemanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.expensemanager"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner =
            "androidx.test.runner.AndroidJUnitRunner"

        vectorDrawables {
            useSupportLibrary = true
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false

            proguardFiles(
                getDefaultProguardFile(
                    "proguard-android-optimize.txt"
                ),
                "proguard-rules.pro"
            )
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }

    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
        compose = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // =============================
    // ANDROID CORE
    // =============================

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    // =============================
    // LIFECYCLE
    // =============================

    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)

    // =============================
    // NAVIGATION
    // =============================

    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    implementation(
        "androidx.navigation:navigation-compose:2.8.1"
    )

    // =============================
    // JETPACK COMPOSE
    // =============================

    implementation(libs.androidx.activity.compose)

    implementation(
        platform(libs.androidx.compose.bom)
    )

    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    implementation(
        "androidx.compose.material:material"
    )

    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    implementation(
        "androidx.constraintlayout:constraintlayout-compose:1.0.1"
    )

    // =============================
    // FIREBASE BoM
    // =============================

    implementation(
        platform("com.google.firebase:firebase-bom:34.7.0")
    )

    // Firebase Authentication
    implementation(
        "com.google.firebase:firebase-auth"
    )

    // Firebase Firestore
    implementation(
        "com.google.firebase:firebase-firestore"
    )

    // Firebase AI Logic (Gemini AI)
    implementation(
        "com.google.firebase:firebase-ai"
    )

    // Firebase App Check Debug
    implementation(
        "com.google.firebase:firebase-appcheck-debug"
    )

    // =============================
    // GOOGLE SIGN-IN
    // =============================

    implementation(
        "com.google.android.gms:play-services-auth:21.2.0"
    )

    // =============================
    // CHARTS
    // =============================

    implementation(
        "com.github.PhilJay:MPAndroidChart:v3.1.0"
    )

    // =============================
    // TENSORFLOW LITE
    // =============================

    implementation(
        "org.tensorflow:tensorflow-lite:2.11.0"
    )

    implementation(
        "org.tensorflow:tensorflow-lite-task-vision:0.4.2"
    )

    // =============================
    // WORKMANAGER
    // =============================

    implementation(
        "androidx.work:work-runtime-ktx:2.8.0"
    )

    // =============================
    // UNIT TESTING
    // =============================

    testImplementation(libs.junit)

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )

    // =============================
    // COMPOSE TESTING
    // =============================

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.ui.test.junit4
    )

    // =============================
    // DEBUGGING
    // =============================

    debugImplementation(
        libs.androidx.ui.tooling
    )

    debugImplementation(
        libs.androidx.ui.test.manifest
    )
}
