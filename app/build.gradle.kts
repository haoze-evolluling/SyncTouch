import org.gradle.api.tasks.Copy

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.haoze.claudekeyboard"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.haoze.claudekeyboard"
        minSdk = 28
        targetSdk = 37
        versionCode = 20030
        versionName = "2.0.3"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    signingConfigs {
        getByName("debug") {
            storeFile = file("keystore/debug.keystore")
            storePassword = "android"
            keyAlias = "androiddebugkey"
            keyPassword = "android"
        }
    }

    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("debug")
            isMinifyEnabled = true
            isShrinkResources = true
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
    buildFeatures {
        compose = true
    }
}

val apkVersionName = android.defaultConfig.versionName ?: "unknown"

listOf("debug", "release").forEach { buildType ->
    val capitalizedBuildType = buildType.replaceFirstChar { it.uppercase() }
    val apkOutputDirectory = layout.buildDirectory.dir("outputs/apk/$buildType")
    val versionedApkOutputDirectory = layout.buildDirectory.dir("outputs/apk/versioned/$buildType")

    val copyApkTask = tasks.register<Copy>("copy${capitalizedBuildType}ApkWithVersion") {
        dependsOn("assemble$capitalizedBuildType")
        from(apkOutputDirectory)
        include("app-$buildType.apk")
        rename("app-$buildType.apk", "SyncTouch-$buildType-v$apkVersionName.apk")
        into(versionedApkOutputDirectory)
    }

    tasks.configureEach {
        if (name == "assemble$capitalizedBuildType") {
            finalizedBy(copyApkTask)
        }
    }
}

dependencies {
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.lifecycle.livedata.ktx)
    implementation(libs.material)
    implementation(libs.androidx.recyclerview)
    implementation(libs.androidx.core.splashscreen)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    debugImplementation(libs.androidx.compose.ui.tooling)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.junit)
}
