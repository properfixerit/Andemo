import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

val localProperties = Properties().apply {
    rootProject.file("local.properties").takeIf { it.isFile }?.inputStream()?.use {
        load(it)
    }
}

fun localConfig(name: String): String =
    localProperties.getProperty(name).orEmpty()
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")

android {
    namespace = "com.ijtc.rubelapp"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.ijtc.rubelapp"
        minSdk = 24
        targetSdk = 37
        versionCode = 16
        versionName = "1.4"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        buildConfigField("String", "RUBELAPP_PROXY_URL", "\"${localConfig("supabase.rubelapp.url")}\"")
        buildConfigField(
            "String",
            "RUBELAPP_PROXY_API_KEY",
            "\"${localConfig("supabase.rubelapp.publishableKey")}\""
        )
    }

    buildTypes {
        release {
            optimization {
                enable = true
                packageScope = setOf("androidx.**", "kotlin.**", "kotlinx.**")
            }
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    buildFeatures {
        compose = true
        buildConfig = true
    }
}

dependencies {
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    testImplementation(libs.junit)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    debugImplementation(libs.androidx.compose.ui.tooling)
}

val testDebugUnitTestTasks = tasks.matching { it.name == "testDebugUnitTest" }
val skipInstallAndRun = providers.gradleProperty("skipInstallAndRun")
    .map(String::toBoolean)
    .getOrElse(false)
val installAndRunDebugTask = tasks.register<Exec>("installAndRunDebug") {
    dependsOn("assembleDebug")
    enabled = !skipInstallAndRun
    workingDir(rootProject.projectDir)
    commandLine(
        "cmd.exe",
        "/c",
        "call",
        "\"${rootProject.file("Install-Update-Run.bat").absolutePath}\""
    )
}

tasks.configureEach {
    if (name == "assembleDebug") {
        dependsOn(testDebugUnitTestTasks)
        finalizedBy(installAndRunDebugTask)
    }
}