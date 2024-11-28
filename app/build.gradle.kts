import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties
import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "com.example.application"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.application"
        minSdk = 21
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        val localProperties = Properties()
        val localPropertiesFile = rootProject.file("local.properties")
        if (localPropertiesFile.exists()) {
            localProperties.load(localPropertiesFile.inputStream())
        }

        // local.properties에서 키를 읽어와 Gradle 속성으로 설정
        val gMapApiKey = localProperties.getProperty("GOOGLE_MAPS_API_KEY") ?: ""

        resValue("string", "google_maps_api_key", gMapApiKey)

        buildConfigField("String", "YOUTUBE_API_KEY", localProperties.getProperty("YOUTUBE_API_KEY") ?: "")
        buildConfigField("String", "AI_API_KEY", localProperties.getProperty("AI_API_KEY") ?: "")
        buildConfigField("String", "PET_FRIENDLY_FACILITIES_DATA_API_KEY", localProperties.getProperty("PET_FRIENDLY_FACILITIES_DATA_API_KEY") ?: "")

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
        buildConfig = true
    }
}

dependencies {

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.constraintlayout.compose.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    // 로딩 화면 GIF 관련
    implementation("pl.droidsonroids.gif:android-gif-drawable:1.2.23")
    // 로딩 화면 시설 데이터 삽입 관련
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4")
    // MaterialCalendarView 관련
    implementation("com.github.prolificinteractive:material-calendarview:2.0.1")
    // 이미지 쉽게 로드하기 위한 Glide 추가
    implementation("com.github.bumptech.glide:glide:4.15.1")
    annotationProcessor("com.github.bumptech.glide:compiler:4.15.1")
    // 유튜브 플레이어 뷰
    implementation("com.pierfrancescosoffritti.androidyoutubeplayer:core:12.1.0")
    // 유튜브 / ChatGPT / 지도 API 관련
    implementation("com.squareup.retrofit2:retrofit:2.9.0")
    implementation("com.squareup.retrofit2:converter-gson:2.9.0")
    // 구글맵
    implementation("com.google.android.gms:play-services-maps:19.0.0")
    implementation("com.google.android.gms:play-services-location:21.3.0")
    implementation("com.google.maps.android:android-maps-utils:3.9.0")
    // API 로그 확인용
    implementation("com.squareup.okhttp3:logging-interceptor:4.9.3")
}