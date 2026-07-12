import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import java.util.Properties

plugins {
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.kotlinAndroid)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
}

kotlin {
    compilerOptions {
        jvmTarget = JvmTarget.JVM_11
    }
}
dependencies {
    implementation(projects.shared)

    implementation(libs.androidx.activity.compose)
    // 카카오 로그인 (키는 local.properties의 kakao.native.app.key)
    implementation(libs.kakao.user)

    implementation(libs.compose.uiToolingPreview)
    debugImplementation(libs.compose.uiTooling)
}

// local.properties 로더 (커밋 제외 파일 — 키/서버주소/전환 플래그 주입)
val localProps = Properties().apply {
    val f = rootProject.file("local.properties")
    if (f.exists()) f.inputStream().use { load(it) }
}
// 카카오 네이티브 앱 키 — `kakao.native.app.key=...` 로 넣으면 실제 로그인 활성화
val kakaoNativeAppKey: String = localProps.getProperty("kakao.native.app.key", "")
// 서버 통합 — `server.base.url`(기본 에뮬레이터→호스트 로컬 서버). 앱은 항상 서버 연결.
val serverBaseUrl: String = localProps.getProperty("server.base.url", "http://10.0.2.2:8080")

android {
    namespace = "com.damoim.app"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        applicationId = "com.damoim.app"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
        manifestPlaceholders["KAKAO_NATIVE_APP_KEY"] = kakaoNativeAppKey
        buildConfigField("String", "KAKAO_NATIVE_APP_KEY", "\"$kakaoNativeAppKey\"")
        buildConfigField("String", "SERVER_BASE_URL", "\"$serverBaseUrl\"")
    }
    buildFeatures {
        buildConfig = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = false
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
}