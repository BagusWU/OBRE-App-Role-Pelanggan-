plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsKotlinAndroid)
    id("com.google.gms.google-services")
    alias(libs.plugins.googleAndroidLibrariesMapsplatformSecretsGradlePlugin)
}

android {
    namespace = "com.obre"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.obre"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
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
    kotlinOptions {
        jvmTarget = "1.8"
    }

    buildFeatures {
        viewBinding = true
    }
}


dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)

    implementation(libs.firebase.auth)
    implementation(libs.firebase.firestore)

    implementation(libs.androidx.activity.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx)

    implementation ("com.google.android.gms:play-services-location:18.0.0")
    implementation ("androidx.fragment:fragment-ktx:1.7.1")
    implementation(libs.androidx.lifecycle.viewmodel.compose)

    implementation (libs.picasso)

    implementation(platform("com.google.firebase:firebase-bom:33.0.0"))
    implementation(libs.google.firebase.storage)
    implementation(libs.play.services.maps)

    implementation(libs.glide)
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.firebase.database.ktx)
    annotationProcessor(libs.compiler)

    androidTestImplementation("androidx.test:runner:1.5.2")
    androidTestImplementation("androidx.test:rules:1.5.0")

    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)

}

