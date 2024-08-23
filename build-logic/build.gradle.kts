plugins {
    `kotlin-dsl`
    `maven-publish`
    id("com.gradle.plugin-publish") version "1.2.1"
}

group = "io.github.qamarelsafadi"
version = "1.3.0"

gradlePlugin {
    website = "https://github.com/MahmoudRH/KMPAppIconGeneratorPlugin"
    vcsUrl = "https://github.com/MahmoudRH/KMPAppIconGeneratorPlugin.git"
    plugins {
        register("io.github.qamarelsafadi.generator.KMPAppIconGeneratorPlugin") {
            id = "io.github.qamarelsafadi.kmp.app.icon.generator"
            implementationClass = "com.qamar.icon.generator.KMPAppIconGeneratorPlugin"
            displayName = "KMPAppIconGeneratorPlugin"
            description = "KMPAppIconGeneratorPlugin is a plugin that helps you change your Android and iOS app icon from one line command"
            tags = listOf("Android", "KMP" , "Kotlin", "CM")
        }
    }
}
dependencies {
    implementation(gradleApi())
    compileOnly(libs.kotlin.gradle.plugin)
}

publishing {
    repositories {
        mavenLocal()
        maven {
            name = "localPluginRepository"
            url = uri("../local-plugin-repository")
        }
    }
}

java {
    sourceCompatibility = JavaVersion.VERSION_19
    targetCompatibility = JavaVersion.VERSION_19
}

kotlin {
    jvmToolchain(19)
}