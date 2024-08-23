package com.qamar.icon.generator

import org.gradle.api.Plugin
import org.gradle.api.Project
import java.awt.AlphaComposite
import java.awt.Image
import java.awt.RenderingHints
import java.awt.geom.Ellipse2D
import java.awt.image.BufferedImage
import java.io.File
import javax.imageio.ImageIO

class KMPAppIconGeneratorPlugin : Plugin<Project> {

    override fun apply(project: Project) {
        project.tasks.register("generateIcons") {
            group = "KMPAppIconGeneratorPlugin"
            description = "Generates Android and iOS icons from a single source image."

            doLast {
                val sourceImagePath = "${project.rootDir}/composeApp/src/commonMain/composeResources/drawable/icon.png"
                val androidResDir = "${project.rootDir}/composeApp/src/androidMain/res"
                val iosResDir = "${project.rootDir}/iosApp/iosApp/Assets.xcassets/AppIcon.appiconset"

                // Ensure source image exists
                val sourceImageFile = File(sourceImagePath)
                if (!sourceImageFile.exists()) {
                    throw IllegalArgumentException("Source image file not found at $sourceImagePath")
                }
                // Resize and generate Android icons
                val androidResolutions = mapOf(
                    "mipmap-mdpi" to 48,
                    "mipmap-hdpi" to 72,
                    "mipmap-xhdpi" to 96,
                    "mipmap-xxhdpi" to 144,
                    "mipmap-xxxhdpi" to 192
                )
                // Function to remove existing PNG files
                fun removePngFiles(directory: File) {
                    directory.listFiles()?.filter { it.extension == "png" || it.extension =="webp" }?.forEach { file ->
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

                androidResolutions.forEach { (folder, size) ->
                    val outputDir = File("$androidResDir/$folder")
                    if (!outputDir.exists()) outputDir.mkdirs()
                    resizeAndSaveImage(sourceImageFile, size, size, File(outputDir, "ic_launcher.png"))
                    resizeAndSaveImage(sourceImageFile, size, size, File(outputDir, "ic_launcher_foreground.png"))
                    resizeAndSaveImage(sourceImageFile, size, size, File(outputDir, "ic_launcher_round.png"), true)
                }

                // Generate iOS icons (Assuming specific resolutions required by iOS)
                val iosResolutions = listOf(
                    20, 29, 40, 58, 60, 76, 80, 87, 120, 152, 167, 180, 1024
                )

                iosResolutions.forEach { size ->
                    val outputDir = File(iosResDir)
                    if (!outputDir.exists()) outputDir.mkdirs()
                    resizeAndSaveImage(sourceImageFile, size, size, File(outputDir, "${size}.png"))
                }
            }
        }
    }

    private fun resizeAndSaveImage(inputFile: File, width: Int, height: Int, outputFile: File, isRounded: Boolean = false) {
        val originalImage: BufferedImage = ImageIO.read(inputFile)
        val resizedImage = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
        val graphics = resizedImage.createGraphics()

        graphics.drawImage(originalImage.getScaledInstance(width, height, Image.SCALE_SMOOTH), 0, 0, null)

        if (isRounded) {
            val mask = BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB)
            val g2 = mask.createGraphics()
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)
            g2.fill(Ellipse2D.Double(0.0, 0.0, width.toDouble(), height.toDouble()))
            g2.dispose()

            graphics.composite = AlphaComposite.DstIn
            graphics.drawImage(mask, 0, 0, null)
        }

        graphics.dispose()

        ImageIO.write(resizedImage, "png", outputFile)
    }
}
