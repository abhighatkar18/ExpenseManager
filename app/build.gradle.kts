
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
        compose = true

        // Keep until all existing XML screens
        // have been migrated to Jetpack Compose.
        viewBinding = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {

    // =====================================
    // 1. ANDROID CORE
    // =====================================

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)


    // =====================================
    // 2. LIFECYCLE & VIEWMODEL
    // =====================================

    implementation(
        libs.androidx.lifecycle.livedata.ktx
    )

    implementation(
        libs.androidx.lifecycle.viewmodel.ktx
    )

    implementation(
        libs.androidx.lifecycle.runtime.ktx
    )

    implementation(
        "androidx.lifecycle:lifecycle-viewmodel-compose:2.8.6"
    )


    // =====================================
    // 3. NAVIGATION
    // =====================================

    // Legacy navigation dependencies
    implementation(
        libs.androidx.navigation.fragment.ktx
    )

    implementation(
        libs.androidx.navigation.ui.ktx
    )

    // Jetpack Compose Navigation
    implementation(
        "androidx.navigation:navigation-compose:2.8.1"
    )


    // =====================================
    // 4. JETPACK COMPOSE
    // =====================================

    implementation(
        libs.androidx.activity.compose
    )

    // Compose Bill of Materials
    implementation(
        platform(libs.androidx.compose.bom)
    )

    // Compose UI
    implementation(libs.androidx.ui)

    implementation(
        libs.androidx.ui.graphics
    )

    implementation(
        libs.androidx.ui.tooling.preview
    )

    // Material Design 3
    implementation(
        libs.androidx.material3
    )

    // Compose Material
    implementation(
        "androidx.compose.material:material"
    )

    // Material Icons
    implementation(
        "androidx.compose.material:material-icons-extended"
    )

    // ConstraintLayout for Compose
    implementation(
        "androidx.constraintlayout:constraintlayout-compose:1.0.1"
    )


    // =====================================
    // 5. FIREBASE
    // =====================================

    // Firebase Bill of Materials
    implementation(
        platform("com.google.firebase:firebase-bom:34.7.0")
    )

    // Firebase Authentication
    implementation(
        "com.google.firebase:firebase-auth"
    )

    // Cloud Firestore
    implementation(
        "com.google.firebase:firebase-firestore"
    )

    // Firebase AI Logic - Gemini
    implementation(
        "com.google.firebase:firebase-ai"
    )


    // =====================================
    // 6. FIREBASE APP CHECK
    // =====================================

    // Debug provider for development
    debugImplementation(
        "com.google.firebase:firebase-appcheck-debug"
    )

    // Play Integrity provider for production
    implementation(
        "com.google.firebase:firebase-appcheck-playintegrity"
    )


    // =====================================
    // 7. GOOGLE SIGN-IN
    // =====================================

    implementation(
        "com.google.android.gms:play-services-auth:21.2.0"
    )


    // =====================================
    // 8. CHARTS & ANALYTICS
    // =====================================

    // Existing MPAndroidChart library
    implementation(
        "com.github.PhilJay:MPAndroidChart:v3.1.0"
    )


    // =====================================
    // 9. TENSORFLOW LITE
    // =====================================

    implementation(
        "org.tensorflow:tensorflow-lite:2.11.0"
    )

    implementation(
        "org.tensorflow:tensorflow-lite-task-vision:0.4.2"
    )


    // =====================================
    // 10. BACKGROUND TASKS
    // =====================================

    // WorkManager for reminders
    implementation(
        "androidx.work:work-runtime-ktx:2.8.0"
    )


    // =====================================
    // 11. UNIT TESTING
    // =====================================

    testImplementation(
        libs.junit
    )

    androidTestImplementation(
        libs.androidx.junit
    )

    androidTestImplementation(
        libs.androidx.espresso.core
    )


    // =====================================
    // 12. COMPOSE TESTING
    // =====================================

    androidTestImplementation(
        platform(libs.androidx.compose.bom)
    )

    androidTestImplementation(
        libs.androidx.ui.test.junit4
    )


    // =====================================
    // 13. DEBUGGING
    // =====================================

    debugImplementation(
        libs.androidx.ui.tooling
    )

    debugImplementation(
        libs.androidx.ui.test.manifest
    )
}
