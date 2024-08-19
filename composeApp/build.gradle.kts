import org.jetbrains.compose.desktop.application.dsl.TargetFormat
import org.jetbrains.kotlin.gradle.ExperimentalKotlinGradlePluginApi
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.jetbrainsCompose)
    alias(libs.plugins.compose.compiler)
    id("com.qamar.icon.generator.module") version "1.0.0"
}

//tasks.register("generateAppIcons") {
//    doLast {
//        val androidIconDir = "$rootDir/composeApp/src/commonMain/composeResources/files/icon/android/"
//        val iosIconDir = "$rootDir/composeApp/src/commonMain/composeResources/files/icon/ios/Assets.xcassets/AppIcon.appiconset"
//        val androidResDir = "$rootDir/composeApp/src/androidMain/res"
//        val iosResDir = "$rootDir/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"
//
//        // Function to remove existing PNG files
//        fun removePngFiles(directory: File) {
//            directory.listFiles()?.filter { it.extension == "png" }?.forEach { file ->
//                file.delete()
//            }
//        }
//
//        // Remove existing PNG files from Android resource directories
//        val androidDirectories = listOf("mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi")
//        androidDirectories.forEach { dir ->
//            val fullPath = file("$androidResDir/$dir")
//            if (fullPath.exists()) {
//                removePngFiles(fullPath)
//            }
//        }
//
//        // Copy Android icons
//        file(androidIconDir).listFiles()?.forEach { file ->
//            file.copyRecursively(file("$androidResDir/${file.name}"), overwrite = true)
//        }
//
//        // Copy iOS icons
//        file(iosIconDir).copyRecursively(file(iosResDir), overwrite = true)
//    }
//}
//
//tasks.getByName("preBuild").dependsOn("generateAppIcons")

kotlin {
    androidTarget {
        @OptIn(ExperimentalKotlinGradlePluginApi::class)
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_20)
        }
    }
    
    listOf(
        iosX64(),
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        
        androidMain.dependencies {
            implementation(compose.preview)
            implementation(libs.androidx.activity.compose)
        }
        commonMain.dependencies {
            implementation(compose.runtime)
            implementation(compose.foundation)
            implementation(compose.material)
            implementation(compose.ui)
            implementation(compose.components.resources)
            implementation(compose.components.uiToolingPreview)
            implementation(libs.androidx.lifecycle.viewmodel)
            implementation(libs.androidx.lifecycle.runtime.compose)
        }
    }
}

android {
    namespace = "com.qamar.icon.generator"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    sourceSets["main"].manifest.srcFile("src/androidMain/AndroidManifest.xml")
    sourceSets["main"].res.srcDirs("src/androidMain/res")
    sourceSets["main"].resources.srcDirs("src/commonMain/resources")

    defaultConfig {
        applicationId = "com.qamar.icon.generator"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 1
        versionName = "1.0"
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
        sourceCompatibility = JavaVersion.VERSION_20
        targetCompatibility = JavaVersion.VERSION_20
    }
    buildFeatures {
        compose = true
    }
    dependencies {
        debugImplementation(compose.uiTooling)
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_20 // Match the consumer project's Java version
    targetCompatibility = JavaVersion.VERSION_20
}

