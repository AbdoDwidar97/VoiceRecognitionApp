plugins {
    id("com.android.library")
}

android {
    namespace = "com.example.voicerecognition"
    compileSdk = 33

    defaultConfig {
        minSdk = 24

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

    implementation ("com.google.cloud:google-cloud-speech:1.29.1")
    implementation ("com.google.auth:google-auth-library-oauth2-http:0.26.0")
    implementation ("io.grpc:grpc-okhttp:1.38.1")
    implementation ("io.grpc:grpc-stub:1.38.1")
    implementation ("com.google.api:gax:1.58.0")
}