package com.qamar.icon.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.io.File

class KMPAppIconGeneratorPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        project.tasks.register("generateAppIcons") {
            group = "KMPAppIconGeneratorPlugin"
            description = "KMPAppIconGeneratorPlugin"
            doLast {
                val androidIconDir = "${project.rootDir}/composeApp/src/commonMain/composeResources/files/icon/android/"
                val iosIconDir = "${project.rootDir}/composeApp/src/commonMain/composeResources/files/icon/ios/Assets.xcassets/AppIcon.appiconset"
                val androidResDir = "${project.rootDir}/composeApp/src/androidMain/res"
                val iosResDir = "${project.rootDir}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"

                // Function to remove existing PNG files
                fun removePngFiles(directory: File) {
                    directory.listFiles()?.filter { it.extension == "png" }?.forEach { file ->
                        file.delete()
                    }
                }

                // Remove existing PNG files from Android resource directories
                val androidDirectories = listOf("mipmap-mdpi", "mipmap-hdpi", "mipmap-xhdpi", "mipmap-xxhdpi", "mipmap-xxxhdpi")
                androidDirectories.forEach { dir ->
                    val fullPath = File("$androidResDir/$dir")
                    if (fullPath.exists()) {
                        removePngFiles(fullPath)
                    }
                }

                // Copy Android icons
                File(androidIconDir).listFiles()?.forEach { file ->
                    file.copyRecursively(File("$androidResDir/${file.name}"), overwrite = true)
                }

                // Copy iOS icons
                File(iosIconDir).copyRecursively(File(iosResDir), overwrite = true)
            }
        }
    }
}