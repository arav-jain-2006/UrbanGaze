plugins {
    alias(libs.plugins.android.application)

    id("com.google.gms.google-services")
}

android {
    namespace = "com.urbangaze.app"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        applicationId = "com.urbangaze.app"
        minSdk = 30
        targetSdk = 36
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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}

dependencies {
    implementation(platform("com.google.firebase:firebase-bom:34.12.0"))
    implementation("com.google.firebase:firebase-firestore")
    implementation("com.google.firebase:firebase-auth")

    //OlaMap SDK
    implementation(files("libs/OlaMapSdk-1.8.4.aar"))
    implementation ("org.maplibre.gl:android-sdk:11.13.1")
    implementation ("org.maplibre.gl:android-plugin-annotation-v9:3.0.2")
    implementation ("org.maplibre.gl:android-plugin-markerview-v9:3.0.2")

    // Location Services
    implementation("com.google.android.gms:play-services-location:21.3.0")

    //Ola Places SDK
    implementation(files("libs/Places-sdk-2.4.0.jar"))
    implementation("com.squareup.retrofit2:converter-gson:2.11.0")
    implementation("com.squareup.retrofit2:retrofit:2.11.0")

    // Supabase
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    
    
    implementation("com.github.bumptech.glide:glide:4.16.0")
    annotationProcessor("com.github.bumptech.glide:compiler:4.16.0")

    // Glide Transformations
    implementation("jp.wasabeef:glide-transformations:4.3.0")

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.navigation.fragment.ktx)
    implementation(libs.navigation.ui.ktx)
    implementation(libs.credentials)
    implementation(libs.credentials.play.services.auth)
    implementation(libs.googleid)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
}