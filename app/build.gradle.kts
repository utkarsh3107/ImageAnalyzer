plugins {
    alias(libs.plugins.androidApplication)
}

android {
    namespace = "com.example.imageanalyzer"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.imageanalyzer"
        minSdk = 28
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
    buildFeatures {
        mlModelBinding = true
    }
}

dependencies {
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.sqlite)
    implementation(libs.exifinterface)
    implementation("androidx.vectordrawable:vectordrawable-animated:1.1.0")
    implementation("androidx.core:core-splashscreen:1.0.1")
    implementation ("org.tensorflow:tensorflow-lite:2.14.0")
    implementation ("org.tensorflow:tensorflow-lite-gpu:2.14.0")
    implementation ("org.tensorflow:tensorflow-lite-support:0.4.4")
    implementation ("org.tensorflow:tensorflow-lite-metadata:0.4.4")
    implementation ("com.quickbirdstudios:opencv:4.5.3.0")
    implementation ("org.tensorflow:tensorflow-lite-select-tf-ops:2.11.0")
    implementation("cz.adaptech.tesseract4android:tesseract4android:4.8.0")
    //implementation(libs.tensorflow.lite)
    //implementation(libs.tensorflow.lite.support)
    //implementation(libs.tensorflow.lite.metadata)
    //implementation("com.drewnoakes:metadata-extractor:2.19.0")
    implementation(libs.glide)
    annotationProcessor(libs.compiler)
    implementation(libs.gson)
    implementation(libs.metadata.extractor)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}