import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidMultiplatformLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.kotlinSerialization)
}

// ── local.properties → iOS 설정 주입 ─────────────────────────────────────────────
// androidApp이 BuildConfig로 넣는 값과 **같은 출처**(local.properties)를 iOS도 쓴다.
// configuration cache가 켜져 있으므로(gradle.properties) 설정 시점 raw 파일 읽기 대신
// providers.fileContents로 읽어 CC 입력으로 정식 등록한다 — 그래야 local.properties를
// 고쳤을 때 캐시 엔트리가 무효화된다.
fun Project.localProperty(key: String, default: String): Provider<String> =
    providers.fileContents(rootProject.layout.projectDirectory.file("local.properties"))
        .asText
        .map { text -> Properties().apply { load(text.reader()) }.getProperty(key, "").trim() }
        .filter { it.isNotEmpty() }
        .orElse(default)

// 서버 주소 — 없으면 로컬 서버로 폴백(빌드는 깨지지 않는다).
// ⚠️ iOS ATS가 cleartext http를 막으므로 실기기/시뮬레이터 검증에는 https 주소여야 한다.
val iosServerBaseUrl: Provider<String> = localProperty("server.base.url", "http://localhost:8080")

/**
 * iosMain에 서버 주소 상수를 생성한다(안드로이드 BuildConfig.SERVER_BASE_URL 대응).
 * xcconfig에 URL을 넣지 않는 이유: xcconfig는 '//'를 주석으로 해석해 https:// 가 깨진다.
 */
val generateIosBuildConfig by tasks.registering {
    group = "ios"
    description = "local.properties의 server.base.url을 iosMain의 IosBuildConfig로 생성한다."
    val serverBaseUrl = iosServerBaseUrl
    val outputDir = layout.buildDirectory.dir("generated/iosBuildConfig/kotlin")
    inputs.property("serverBaseUrl", serverBaseUrl)
    outputs.dir(outputDir)
    doLast {
        // Kotlin 문자열 리터럴 이스케이프(역슬래시 → 따옴표 → 달러 순).
        val url = serverBaseUrl.get()
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\$", "\\\$")
        val pkgDir = outputDir.get().asFile.resolve("com/damoim/app")
        pkgDir.mkdirs()
        pkgDir.resolve("IosBuildConfig.kt").writeText(
            """
            package com.damoim.app

            // ⚠️ 자동 생성 파일 — 수정 금지. 출처: local.properties의 server.base.url
            // (Gradle :shared:generateIosBuildConfig 가 생성한다.)
            internal object IosBuildConfig {
                const val SERVER_BASE_URL: String = "$url"
            }

            """.trimIndent()
        )
    }
}

kotlin {
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "Shared"
            isStatic = true
        }
    }
    
    androidLibrary {
       namespace = "com.damoim.app.shared"
       compileSdk = libs.versions.android.compileSdk.get().toInt()
       minSdk = libs.versions.android.minSdk.get().toInt()
    
       compilerOptions {
           jvmTarget = JvmTarget.JVM_11
       }
       androidResources {
           enable = true
       }
       withHostTest {
           isIncludeAndroidResources = true
       }
    }
    
    sourceSets {
        androidMain.dependencies {
            // 카메라/문서 피커(ActivityResult)·시스템 백 처리용
            implementation(libs.androidx.activity.compose)
            // Ktor 엔진 (Android) — 명시 엔진 없이 HttpClient{}가 자동 선택
            implementation(libs.ktor.client.okhttp)
            implementation(libs.compose.uiToolingPreview)
            // @Preview 렌더링에 필요한 ComposeViewAdapter(androidx ui-tooling)를 android 컴파일+런타임
            // 클래스패스에 올린다. 이 KMP android 라이브러리 플러그인은 debug/release 변형 구분이 없어
            // debugImplementation을 쓸 수 없으므로 androidMain implementation으로 둔다.
            // (release APK에도 포함되어 크기가 커짐 — 배포 직전에 제거하거나 별도 구성 검토)
            implementation(libs.compose.uiTooling)
        }
        commonMain.dependencies {
            implementation(libs.compose.runtime)
            implementation(libs.compose.foundation)
            implementation(libs.compose.material3)
            implementation(libs.compose.ui)
            implementation(libs.compose.components.resources)
            implementation(libs.compose.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodelCompose)
            implementation(libs.androidx.lifecycle.runtimeCompose)
            // 사진 피커 (Android + iOS) — 이미지 선택 후 ByteArray 반환 + toImageBitmap 제공
            implementation(libs.peekaboo.imagepicker)
            // 날짜/시간 (KMP) — 날짜 피커·D-day 계산
            implementation(libs.kotlinx.datetime)
            // Ktor (서버 통합) — HTTP 클라이언트 + JSON 직렬화 + 인증(refresh)
            implementation(libs.ktor.client.core)
            implementation(libs.ktor.client.contentNegotiation)
            implementation(libs.ktor.serialization.json)
            implementation(libs.ktor.client.auth)
            implementation(libs.ktor.client.logging)
            implementation(libs.kotlinx.serialization.json)
        }
        iosMain {
            // 생성 소스(IosBuildConfig) 등록. TaskProvider를 넘기면 srcDir 등록과 태스크 의존성이
            // 한번에 걸려 첫 빌드에서도 Unresolved reference가 나지 않는다.
            kotlin.srcDir(generateIosBuildConfig)
            dependencies {
                // Ktor 엔진 (iOS/Darwin)
                implementation(libs.ktor.client.darwin)
            }
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

